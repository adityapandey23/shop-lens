package tech.thedumbdev.shop_lens.service.impl;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.thedumbdev.shop_lens.config.RabbitMQConfig;
import tech.thedumbdev.shop_lens.dto.*;
import tech.thedumbdev.shop_lens.model.*;
import tech.thedumbdev.shop_lens.model.enums.FinancialStatus;
import tech.thedumbdev.shop_lens.model.enums.FulfillmentStatus;
import tech.thedumbdev.shop_lens.model.enums.SyncStatusType;
import tech.thedumbdev.shop_lens.repository.*;
import tech.thedumbdev.shop_lens.service.IngestionService;
import tech.thedumbdev.shop_lens.service.QueueService;
import tech.thedumbdev.shop_lens.service.ShopifyDataService;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class IngestionServiceImpl implements IngestionService {

    private final ShopifyDataService shopifyDataService;
    private final QueueService queueService;
    private final SyncJobRepo syncJobRepo;
    private final SyncCheckpointRepo syncCheckpointRepo;
    private final TenantRepo tenantRepo;
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final CustomerRepo customerRepo;
    private final IngestionServiceImpl self;  // Self-injection for proxy calls

    public IngestionServiceImpl(
            ShopifyDataService shopifyDataService,
            QueueService queueService,
            SyncJobRepo syncJobRepo,
            SyncCheckpointRepo syncCheckpointRepo,
            TenantRepo tenantRepo,
            OrderRepo orderRepo,
            ProductRepo productRepo,
            CustomerRepo customerRepo,
            @Lazy IngestionServiceImpl self  // Lazy to avoid circular dependency
    ) {
        this.shopifyDataService = shopifyDataService;
        this.queueService = queueService;
        this.syncJobRepo = syncJobRepo;
        this.syncCheckpointRepo = syncCheckpointRepo;
        this.tenantRepo = tenantRepo;
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
        this.self = self;
    }

    @Override
    @RabbitListener(queues = RabbitMQConfig.SHOPIFY_SYNC_QUEUE)
    public void processSyncMessage(ShopifySyncMessage message) {
        // Delegate to transactional method through proxy - this ensures @Transactional works
        // because it goes through the Spring proxy via self-injection
        self.doProcessSyncMessage(message);
    }

    @Transactional
    public void doProcessSyncMessage(ShopifySyncMessage message) {
        SyncJob job = syncJobRepo.findById(message.jobId())
                .orElseThrow(() -> new RuntimeException("SyncJob not found: " + message.jobId()));

        // Get or create checkpoint for this job
        SyncCheckpoint checkpoint = getOrCreateCheckpoint(job);

        try {
            // Update status to IN_PROGRESS
            checkpoint.setStatus(SyncStatusType.IN_PROGRESS);
            job.setStatus(SyncStatusType.IN_PROGRESS);
            syncCheckpointRepo.save(checkpoint);
            syncJobRepo.save(job);

            // Fetch page from Shopify
            ShopifyPageResult<?> result = shopifyDataService.fetchPage(
                    message.shopDomain(),
                    message.accessToken(),
                    message.cursor(),
                    message.entityType()
            );

            // Save fetched data
            if (result.data() != null && !result.data().isEmpty()) {
                shopifyDataService.savePageData(
                        result.data(),
                        message.entityType(),
                        message.tenantId()
                );

                // Update checkpoint progress
                int currentPages = checkpoint.getTotalPagesProcessed() != null
                        ? checkpoint.getTotalPagesProcessed() : 0;
                int currentRecords = checkpoint.getTotalRecordsProcessed() != null
                        ? checkpoint.getTotalRecordsProcessed() : 0;

                checkpoint.setTotalPagesProcessed(currentPages + 1);
                checkpoint.setTotalRecordsProcessed(currentRecords + result.data().size());
                checkpoint.setLastCursor(result.getNextCursor());

            }

            // Check if there are more pages to fetch
            if (result.hasNextPage() && result.getNextCursor() != null) {
                // Queue next page
                ShopifySyncMessage nextMessage = new ShopifySyncMessage(
                        message.jobId(),
                        message.tenantId(),
                        message.shopDomain(),
                        message.accessToken(),
                        result.getNextCursor(),
                        message.entityType()
                );
                queueService.publishSyncMessage(nextMessage);
            } else {
                // Sync completed
                checkpoint.setStatus(SyncStatusType.COMPLETED);
                job.setStatus(SyncStatusType.COMPLETED);
                syncJobRepo.save(job);
            }

            syncCheckpointRepo.save(checkpoint);

        } catch (Exception e) {

            // Update checkpoint with error
            checkpoint.setStatus(SyncStatusType.FAILED);
            checkpoint.setErrorMessage(truncateErrorMessage(e.getMessage()));
            syncCheckpointRepo.save(checkpoint);

            // Update job status
            job.setStatus(SyncStatusType.FAILED);
            syncJobRepo.save(job);

            // Re-throw to trigger DLQ
            throw new RuntimeException("Sync failed for job: " + message.jobId(), e);
        }
    }

    private SyncCheckpoint getOrCreateCheckpoint(SyncJob job) {
        return syncCheckpointRepo.findByJobId(job.getId())
                .orElseGet(() -> {
                    SyncCheckpoint newCheckpoint = new SyncCheckpoint();
                    newCheckpoint.setJob(job);
                    newCheckpoint.setStatus(SyncStatusType.IN_PROGRESS);
                    newCheckpoint.setTotalPagesProcessed(0);
                    newCheckpoint.setTotalRecordsProcessed(0);
                    return syncCheckpointRepo.save(newCheckpoint);
                });
    }

    private String truncateErrorMessage(String message) {
        if (message == null) return "Unknown error";
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }

    @Override
    @Transactional
    public StartSyncResponse startSync(StartSyncRequest request) {
        // Get tenant
        Tenant tenant = tenantRepo.findById(request.tenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + request.tenantId()));

        // Create new sync job
        SyncJob job = new SyncJob();
        job.setTenant(tenant);
        job.setEntityType(request.entityType());
        job.setStatus(SyncStatusType.IN_PROGRESS);
        job = syncJobRepo.save(job);

        // Create initial message (null cursor = start from beginning/most recent)
        ShopifySyncMessage initialMessage = new ShopifySyncMessage(
                job.getId(),
                tenant.getId(),
                tenant.getShopDomain(),
                tenant.getAccessToken(),
                null,  // null cursor starts from the beginning
                request.entityType()
        );

        // Publish to queue
        queueService.publishSyncMessage(initialMessage);

        return new StartSyncResponse(
                job.getId(),
                tenant.getId(),
                request.entityType(),
                "Sync job started successfully"
        );
    }

    @Override
    public SyncJob getSyncJobStatus(UUID jobId) {
        return syncJobRepo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("SyncJob not found: " + jobId));
    }

    // ==================== Webhook Processing ====================

    @Override
    @Transactional
    public void processOrderWebhook(Tenant tenant, OrderWebhookPayload payload) {
        String shopifyId = payload.adminGraphqlApiId();

        // Find existing or create new
        Order order = orderRepo.findByShopifyIdAndTenant(shopifyId, tenant)
                .orElseGet(() -> {
                    Order newOrder = new Order();
                    newOrder.setTenant(tenant);
                    newOrder.setShopifyId(shopifyId);
                    return newOrder;
                });

        // Update fields
        order.setName(payload.name());
        order.setShopifyCreatedAt(payload.createdAt());

        if (payload.totalPrice() != null) {
            order.setTotalPrice(new BigDecimal(payload.totalPrice()));
        }
        order.setCurrencyCode(payload.currency());

        if (payload.financialStatus() != null) {
            order.setFinancialStatus(FinancialStatus.fromString(payload.financialStatus().toUpperCase()));
        }
        if (payload.fulfillmentStatus() != null) {
            order.setFulfillmentStatus(FulfillmentStatus.fromString(payload.fulfillmentStatus().toUpperCase()));
        }

        // Customer info
        if (payload.customer() != null) {
            order.setCustomerShopifyId(payload.customer().adminGraphqlApiId());
            order.setCustomerEmail(payload.customer().email());
        }

        orderRepo.save(order);
    }

    @Override
    @Transactional
    public void processProductWebhook(Tenant tenant, ProductWebhookPayload payload) {
        String shopifyId = payload.adminGraphqlApiId();

        // Find existing or create new
        Product product = productRepo.findByShopifyIdAndTenant(shopifyId, tenant)
                .orElseGet(() -> {
                    Product newProduct = new Product();
                    newProduct.setTenant(tenant);
                    newProduct.setShopifyId(shopifyId);
                    return newProduct;
                });

        // Update fields
        product.setTitle(payload.title());
        product.setShopifyCreatedAt(payload.createdAt());
        product.setPublishedAt(payload.publishedAt());
        product.setTotalInventory(payload.getTotalInventory());

        String price = payload.getFirstVariantPrice();
        if (price != null) {
            product.setPrice(new BigDecimal(price));
        }

        productRepo.save(product);
    }

    @Override
    @Transactional
    public void processCustomerWebhook(Tenant tenant, CustomerWebhookPayload payload) {
        String shopifyId = payload.adminGraphqlApiId();

        // Find existing or create new
        Customer customer = customerRepo.findByShopifyIdAndTenant(shopifyId, tenant)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setTenant(tenant);
                    newCustomer.setShopifyId(shopifyId);
                    return newCustomer;
                });

        // Update fields
        customer.setEmail(payload.email());
        customer.setFirstName(payload.firstName());
        customer.setLastName(payload.lastName());
        customer.setShopifyCreatedAt(payload.createdAt());
        customer.setOrdersCount(payload.ordersCount());

        if (payload.totalSpent() != null) {
            customer.setAmountSpent(new BigDecimal(payload.totalSpent()));
        }
        customer.setAmountSpentCurrency(payload.currency());

        customerRepo.save(customer);
    }
}
