package tech.thedumbdev.shop_lens.model.enums;

/**
 * Shopify Order Fulfillment Status values.
 * See: https://shopify.dev/docs/api/admin-graphql/latest/enums/OrderDisplayFulfillmentStatus
 */
public enum FulfillmentStatus {
    UNFULFILLED,
    PARTIALLY_FULFILLED,
    FULFILLED,
    RESTOCKED,
    PENDING_FULFILLMENT,
    OPEN,
    IN_PROGRESS,
    ON_HOLD,
    SCHEDULED,
    // Fallback for unknown values
    UNKNOWN;

    public static FulfillmentStatus fromString(String value) {
        if (value == null) return null;
        try {
            return FulfillmentStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
