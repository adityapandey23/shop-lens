package tech.thedumbdev.shop_lens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.SyncCheckpoint;
import tech.thedumbdev.shop_lens.model.SyncJob;

import java.util.Optional;
import java.util.UUID;

public interface SyncCheckpointRepo extends JpaRepository<SyncCheckpoint, UUID> {
    Optional<SyncCheckpoint> findByJob(SyncJob job);
    Optional<SyncCheckpoint> findByJobId(UUID jobId);
}
