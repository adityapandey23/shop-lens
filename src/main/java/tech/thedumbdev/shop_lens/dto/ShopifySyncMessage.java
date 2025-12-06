package tech.thedumbdev.shop_lens.dto;

import tech.thedumbdev.shop_lens.model.enums.EntityType;

import java.io.Serializable;
import java.util.UUID;

public record ShopifySyncMessage(
        UUID jobId,
        UUID tenantId,
        String shopDomain,
        String accessToken,
        String cursor,
        EntityType entityType
) implements Serializable {}
