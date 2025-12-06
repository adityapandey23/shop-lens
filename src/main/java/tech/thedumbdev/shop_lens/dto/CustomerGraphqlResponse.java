package tech.thedumbdev.shop_lens.dto;

import tech.thedumbdev.shop_lens.graphql_objects.customers.CustomerData;

public record CustomerGraphqlResponse(
        CustomerData customers
) {
}
