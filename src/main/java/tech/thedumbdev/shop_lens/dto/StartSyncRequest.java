package tech.thedumbdev.shop_lens.dto;

import tech.thedumbdev.shop_lens.model.enums.EntityType;

import java.util.UUID;

public record StartSyncRequest(
        UUID tenantId,
        EntityType entityType
) {}
