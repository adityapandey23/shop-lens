package tech.thedumbdev.shop_lens.graphql_objects;

public record PageInfo(
        boolean hasNextPage,
        String endCursor
) {
}
