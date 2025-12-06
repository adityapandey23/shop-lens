package tech.thedumbdev.shop_lens.dto;

import java.util.List;

public record ShopifyPageResult<T>(
        List<T> data,
        PageInfo pageInfo
) {
    public record PageInfo(
            boolean hasMorePage,
            String nextCursor
    ) {}

    public boolean hasNextPage() {
        return pageInfo != null && pageInfo.hasMorePage;
    }

    public String getNextCursor() {
        return pageInfo != null ? pageInfo.nextCursor : null;
    }
}
