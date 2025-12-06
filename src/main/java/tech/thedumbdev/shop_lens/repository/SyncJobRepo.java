package tech.thedumbdev.shop_lens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.SyncJob;
import tech.thedumbdev.shop_lens.model.Tenant;
import tech.thedumbdev.shop_lens.model.enums.EntityType;
import tech.thedumbdev.shop_lens.model.enums.SyncStatusType;

import java.util.List;
import java.util.UUID;

public interface SyncJobRepo extends JpaRepository<SyncJob, UUID> {
    List<SyncJob> findByTenantAndStatus(Tenant tenant, SyncStatusType status);
    List<SyncJob> findByTenantAndEntityTypeAndStatus(Tenant tenant, EntityType entityType, SyncStatusType status);
}
