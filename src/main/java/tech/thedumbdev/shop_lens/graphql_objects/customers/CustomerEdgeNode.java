package tech.thedumbdev.shop_lens.graphql_objects.customers;

public record CustomerEdgeNode(
        String id,
        String firstName,
        String lastName,
        String email,
        String createdAt,
        Integer numberOfOrders,  // Changed from ordersCount
        AmountSpent amountSpent,
        LastOrder lastOrder
) {
}
