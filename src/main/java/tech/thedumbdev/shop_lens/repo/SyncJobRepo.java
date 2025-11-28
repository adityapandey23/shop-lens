package tech.thedumbdev.shop_lens.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.SyncJob;

import java.util.UUID;

public interface SyncJobRepo extends JpaRepository<SyncJob, UUID> {
}
