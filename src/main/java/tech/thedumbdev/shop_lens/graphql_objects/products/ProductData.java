package tech.thedumbdev.shop_lens.graphql_objects.products;

import tech.thedumbdev.shop_lens.graphql_objects.PageInfo;

import java.util.List;

public record ProductData(
        List<ProductEdge> edges,
        PageInfo pageInfo
) {
}
