package tech.thedumbdev.shop_lens.graphql_objects.orders;

public record OrderEdgeNode(
        String id,
        String name,
        String createdAt,
        TotalPriceSet totalPriceSet,
        String displayFinancialStatus,
        String displayFulfillmentStatus,
        OrderCustomer customer
) {
}
