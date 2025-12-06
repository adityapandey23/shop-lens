package tech.thedumbdev.shop_lens.graphql_objects.customers;

import tech.thedumbdev.shop_lens.graphql_objects.PageInfo;

import java.util.List;

public record CustomerData(
        List<CustomerEdge> edges,
        PageInfo pageInfo
) {
}
