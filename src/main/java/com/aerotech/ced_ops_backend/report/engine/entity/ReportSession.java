package com.aerotech.ced_ops_backend.report.engine.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import com.aerotech.ced_ops_backend.master.module.entity.Module;
import com.aerotech.ced_ops_backend.master.module.entity.Process;
import com.aerotech.ced_ops_backend.master.module.entity.TemplateVersion;
import com.aerotech.ced_ops_backend.report.engine.enums.ReportSessionStatus;
import com.aerotech.ced_ops_backend.user.entity.User;
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
 * Work in progress. A Report Session freezes the Module's template version at
 * creation time; the template version is never switched while the report is
 * being filled (historical correctness).
 */
@Entity
@Table(name = "report_session", indexes = {
        @Index(name = "idx_report_session_module", columnList = "module_id"),
        @Index(name = "idx_report_session_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    /**
     * The template version frozen when this session was created. Never changed
     * afterwards, even if the module publishes newer versions meanwhile.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_version_id", nullable = false)
    private TemplateVersion templateVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_process_id")
    private Process currentProcess;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_process_count", nullable = false)
    private Integer completedProcessCount;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportSessionStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // ---- Shift/Line captured at start (snapshotted onto the report at submit) ----
    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "shift_name", length = 50)
    private String shiftName;

    @Column(name = "line_id")
    private Long lineId;

    @Column(name = "line_name", length = 100)
    private String lineName;

}