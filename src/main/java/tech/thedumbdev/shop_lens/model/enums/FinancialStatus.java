package tech.thedumbdev.shop_lens.model.enums;

/**
 * Shopify Order Financial Status values.
 * See: https://shopify.dev/docs/api/admin-graphql/latest/enums/OrderDisplayFinancialStatus
 */
public enum FinancialStatus {
    PENDING,
    AUTHORIZED,
    PARTIALLY_PAID,
    PAID,
    PARTIALLY_REFUNDED,
    REFUNDED,
    VOIDED,
    EXPIRED,
    // Fallback for unknown values
    UNKNOWN;

    public static FinancialStatus fromString(String value) {
        if (value == null) return null;
        try {
            return FinancialStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
