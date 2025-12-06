package tech.thedumbdev.shop_lens.dto;

import tech.thedumbdev.shop_lens.graphql_objects.orders.OrderData;

public record OrderGraphqlResponse(
        OrderData orders
) {
}
