package tech.thedumbdev.shop_lens.graphql_objects.products;

import java.util.List;

public record NodeVariant(
        List<VariantEdge> edges
) {
}
