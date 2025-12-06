package tech.thedumbdev.shop_lens.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Shopify Product Webhook Payload.
 * Only includes fields we care about - other fields are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductWebhookPayload(
        Long id,

        @JsonProperty("admin_graphql_api_id")
        String adminGraphqlApiId,

        String title,

        @JsonProperty("created_at")
        String createdAt,

        @JsonProperty("published_at")
        String publishedAt,

        List<VariantInfo> variants
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VariantInfo(
            Long id,
            String price,

            @JsonProperty("inventory_quantity")
            Integer inventoryQuantity
    ) {}

    /**
     * Get the price from the first variant (if available)
     */
    public String getFirstVariantPrice() {
        if (variants != null && !variants.isEmpty()) {
            return variants.getFirst().price();
        }
        return null;
    }

    /**
     * Get total inventory across all variants
     */
    public Integer getTotalInventory() {
        if (variants == null || variants.isEmpty()) {
            return 0;
        }
        return variants.stream()
                .mapToInt(v -> v.inventoryQuantity() != null ? v.inventoryQuantity() : 0)
                .sum();
    }
}
