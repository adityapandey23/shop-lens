package tech.thedumbdev.shop_lens.dto;

import tech.thedumbdev.shop_lens.model.enums.EntityType;

import java.util.UUID;

public record StartSyncResponse(
        UUID jobId,
        UUID tenantId,
        EntityType entityType,
        String message
) {}
