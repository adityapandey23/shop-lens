package tech.thedumbdev.shop_lens.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.thedumbdev.shop_lens.dto.StartSyncRequest;
import tech.thedumbdev.shop_lens.dto.StartSyncResponse;
import tech.thedumbdev.shop_lens.model.SyncCheckpoint;
import tech.thedumbdev.shop_lens.model.SyncJob;
import tech.thedumbdev.shop_lens.repository.SyncCheckpointRepo;
import tech.thedumbdev.shop_lens.service.IngestionService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;
    private final SyncCheckpointRepo syncCheckpointRepo;

    public IngestionController(
            IngestionService ingestionService,
            SyncCheckpointRepo syncCheckpointRepo
    ) {
        this.ingestionService = ingestionService;
        this.syncCheckpointRepo = syncCheckpointRepo;
    }

    /**
     * Start a new sync job for a tenant and entity type
     */
    @PostMapping("/sync/start")
    public ResponseEntity<StartSyncResponse> startSync(@RequestBody StartSyncRequest request) {
        StartSyncResponse response = ingestionService.startSync(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get the status of a sync job
     */
    @GetMapping("/sync/status/{jobId}")
    public ResponseEntity<Map<String, Object>> getSyncStatus(@PathVariable UUID jobId) {
        SyncJob job = ingestionService.getSyncJobStatus(jobId);
        SyncCheckpoint checkpoint = syncCheckpointRepo.findByJobId(jobId).orElse(null);

        return ResponseEntity.ok(Map.of(
                "jobId", job.getId(),
                "entityType", job.getEntityType(),
                "status", job.getStatus(),
                "pagesProcessed", checkpoint != null ? checkpoint.getTotalPagesProcessed() : 0,
                "recordsProcessed", checkpoint != null ? checkpoint.getTotalRecordsProcessed() : 0,
                "lastCursor", checkpoint != null && checkpoint.getLastCursor() != null ? checkpoint.getLastCursor() : "",
                "errorMessage", checkpoint != null && checkpoint.getErrorMessage() != null ? checkpoint.getErrorMessage() : ""
        ));
    }
}
