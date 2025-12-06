package tech.thedumbdev.shop_lens.dto;

import tech.thedumbdev.shop_lens.graphql_objects.products.ProductData;

public record ProductGraphqlResponse(
        ProductData products
) {
}
