package tech.thedumbdev.shop_lens.graphql_objects.orders;

import tech.thedumbdev.shop_lens.graphql_objects.PageInfo;

import java.util.List;

public record OrderData(
        List<OrderEdge> edges,
        PageInfo pageInfo
) {
}
