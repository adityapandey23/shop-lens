package tech.thedumbdev.shop_lens.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Shopify Order Webhook Payload.
 * Only includes fields we care about - other fields are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderWebhookPayload(
        Long id,

        @JsonProperty("admin_graphql_api_id")
        String adminGraphqlApiId,

        String name,

        @JsonProperty("created_at")
        String createdAt,

        @JsonProperty("total_price")
        String totalPrice,

        String currency,

        @JsonProperty("financial_status")
        String financialStatus,

        @JsonProperty("fulfillment_status")
        String fulfillmentStatus,

        CustomerInfo customer
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CustomerInfo(
            Long id,

            @JsonProperty("admin_graphql_api_id")
            String adminGraphqlApiId,

            String email
    ) {}
}
