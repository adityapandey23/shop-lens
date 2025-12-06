package tech.thedumbdev.shop_lens.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Shopify Customer Webhook Payload.
 * Only includes fields we care about - other fields are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerWebhookPayload(
        Long id,

        @JsonProperty("admin_graphql_api_id")
        String adminGraphqlApiId,

        String email,

        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("last_name")
        String lastName,

        @JsonProperty("created_at")
        String createdAt,

        @JsonProperty("orders_count")
        Integer ordersCount,

        @JsonProperty("total_spent")
        String totalSpent,

        String currency
) {
}
