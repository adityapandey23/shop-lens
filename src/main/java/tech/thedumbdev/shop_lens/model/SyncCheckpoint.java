package tech.thedumbdev.shop_lens.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.thedumbdev.shop_lens.model.enums.SyncStatusType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sync_checkpoints")
public class SyncCheckpoint extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private SyncJob job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncStatusType status;

    // Cursor for fetching the next page (null means start from beginning)
    @Column
    private String lastCursor;

    @Column
    private Integer totalPagesProcessed;

    @Column
    private Integer totalRecordsProcessed;

    @Column(length = 1000)
    private String errorMessage;
}
