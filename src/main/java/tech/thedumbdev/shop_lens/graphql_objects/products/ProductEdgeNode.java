package tech.thedumbdev.shop_lens.graphql_objects.products;

public record ProductEdgeNode(
        String id,
        String title,
        Integer totalInventory,
        String publishedAt,
        String createdAt,
        NodeVariant variants
) {
}
