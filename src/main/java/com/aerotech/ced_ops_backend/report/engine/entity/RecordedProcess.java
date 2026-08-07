package com.aerotech.ced_ops_backend.report.engine.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import com.aerotech.ced_ops_backend.master.module.entity.Process;
import com.aerotech.ced_ops_backend.report.engine.enums.RecordedProcessStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A single process of the frozen template version as recorded inside a report
 * session. Groups all of its {@link RecordedValue}s under one entity — the
 * recorded data is never flattened into a single list.
 *
 * <p>The {@code processOrderSnapshot} captures {@code displayOrder} of the
 * template process at the moment it is recorded (process-order snapshot), so a
 * later reordering of the template never affects historical reports.
 */
@Entity
@Table(name = "recorded_process", indexes = {
        @Index(name = "idx_recorded_process_session", columnList = "session_id"),
        @Index(name = "idx_recorded_process_process", columnList = "process_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordedProcess extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ReportSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id", nullable = false)
    private Process process;

    @Column(name = "process_order_snapshot", nullable = false)
    private Integer processOrderSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordedProcessStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

}