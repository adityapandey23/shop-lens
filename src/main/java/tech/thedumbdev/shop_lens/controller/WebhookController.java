package tech.thedumbdev.shop_lens.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.thedumbdev.shop_lens.dto.CustomerWebhookPayload;
import tech.thedumbdev.shop_lens.dto.OrderWebhookPayload;
import tech.thedumbdev.shop_lens.dto.ProductWebhookPayload;
import tech.thedumbdev.shop_lens.model.Tenant;
import tech.thedumbdev.shop_lens.repository.TenantRepo;
import tech.thedumbdev.shop_lens.service.IngestionService;
import tech.thedumbdev.shop_lens.util.WebhookHmacUtil;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final IngestionService ingestionService;
    private final TenantRepo tenantRepo;
    private final ObjectMapper objectMapper;
    private final String apiSecret;

    public WebhookController(
            IngestionService ingestionService,
            TenantRepo tenantRepo,
            ObjectMapper objectMapper,
            @Value("${shopify.api.secret}") String apiSecret
    ) {
        this.ingestionService = ingestionService;
        this.tenantRepo = tenantRepo;
        this.objectMapper = objectMapper;
        this.apiSecret = apiSecret;
    }

    /**
     * Webhook endpoint for order events (orders/create, orders/updated)
     */
    @PostMapping("/orders")
    public ResponseEntity<String> handleOrderWebhook(
            @RequestHeader("X-Shopify-Hmac-Sha256") String hmacHeader,
            @RequestHeader("X-Shopify-Shop-Domain") String shopDomain,
            @RequestHeader("X-Shopify-Topic") String topic,
            @RequestBody String rawBody
    ) {
        // Verify HMAC
        if (!WebhookHmacUtil.isValidHmac(rawBody, hmacHeader, apiSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid HMAC");
        }

        // Find tenant
        Tenant tenant = tenantRepo.findByShopDomain(shopDomain).orElse(null);
        if (tenant == null) {
            return ResponseEntity.ok("Shop not found - ignored");
        }

        try {
            // Parse payload
            OrderWebhookPayload payload = objectMapper.readValue(rawBody, OrderWebhookPayload.class);

            // Process based on topic
            if (topic.equals("orders/create") || topic.equals("orders/updated")) {
                ingestionService.processOrderWebhook(tenant, payload);
            }

            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to parse payload");
        }
    }

    /**
     * Webhook endpoint for product events (products/create, products/update)
     */
    @PostMapping("/products")
    public ResponseEntity<String> handleProductWebhook(
            @RequestHeader("X-Shopify-Hmac-Sha256") String hmacHeader,
            @RequestHeader("X-Shopify-Shop-Domain") String shopDomain,
            @RequestHeader("X-Shopify-Topic") String topic,
            @RequestBody String rawBody
    ) {
        // Verify HMAC
        if (!WebhookHmacUtil.isValidHmac(rawBody, hmacHeader, apiSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid HMAC");
        }

        // Find tenant
        Tenant tenant = tenantRepo.findByShopDomain(shopDomain).orElse(null);
        if (tenant == null) {
            return ResponseEntity.ok("Shop not found - ignored");
        }

        try {
            // Parse payload
            ProductWebhookPayload payload = objectMapper.readValue(rawBody, ProductWebhookPayload.class);

            // Process based on topic
            if (topic.equals("products/create") || topic.equals("products/update")) {
                ingestionService.processProductWebhook(tenant, payload);
            }

            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to parse payload");
        }
    }

    /**
     * Webhook endpoint for customer events (customers/create, customers/update)
     */
    @PostMapping("/customers")
    public ResponseEntity<String> handleCustomerWebhook(
            @RequestHeader("X-Shopify-Hmac-Sha256") String hmacHeader,
            @RequestHeader("X-Shopify-Shop-Domain") String shopDomain,
            @RequestHeader("X-Shopify-Topic") String topic,
            @RequestBody String rawBody
    ) {
        // Verify HMAC
        if (!WebhookHmacUtil.isValidHmac(rawBody, hmacHeader, apiSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid HMAC");
        }

        // Find tenant
        Tenant tenant = tenantRepo.findByShopDomain(shopDomain).orElse(null);
        if (tenant == null) {
            return ResponseEntity.ok("Shop not found - ignored");
        }

        try {
            // Parse payload
            CustomerWebhookPayload payload = objectMapper.readValue(rawBody, CustomerWebhookPayload.class);

            // Process based on topic
            if (topic.equals("customers/create") || topic.equals("customers/update")) {
                ingestionService.processCustomerWebhook(tenant, payload);
            }

            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to parse payload");
        }
    }
}
