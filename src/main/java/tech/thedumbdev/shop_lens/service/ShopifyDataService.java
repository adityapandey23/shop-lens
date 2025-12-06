package tech.thedumbdev.shop_lens.service;

import tech.thedumbdev.shop_lens.dto.ShopifyPageResult;
import tech.thedumbdev.shop_lens.model.enums.EntityType;

import java.util.List;
import java.util.UUID;

public interface ShopifyDataService {
    public ShopifyPageResult<?> fetchPage(
            String shopDomain,
            String accessToken,
            String cursor,
            EntityType entityType
    );

    public void savePageData(List<?> data, EntityType entityType, UUID tenantId);
}
