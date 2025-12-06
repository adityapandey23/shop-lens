package tech.thedumbdev.shop_lens.service;

import tech.thedumbdev.shop_lens.dto.*;
import tech.thedumbdev.shop_lens.model.SyncJob;
import tech.thedumbdev.shop_lens.model.Tenant;

import java.util.UUID;

public interface IngestionService {
    // ==================== Backfill ====================

    // Called by RabbitMQ listener
    void processSyncMessage(ShopifySyncMessage syncMessage);

    // Start a new sync job
    StartSyncResponse startSync(StartSyncRequest request);

    // Get sync job status
    SyncJob getSyncJobStatus(UUID jobId);

    // ==================== Webhooks ====================

    // Process order webhook (create/update)
    void processOrderWebhook(Tenant tenant, OrderWebhookPayload payload);

    // Process product webhook (create/update)
    void processProductWebhook(Tenant tenant, ProductWebhookPayload payload);

    // Process customer webhook (create/update)
    void processCustomerWebhook(Tenant tenant, CustomerWebhookPayload payload);
}
