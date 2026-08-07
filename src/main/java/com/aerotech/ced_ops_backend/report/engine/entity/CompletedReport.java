package com.aerotech.ced_ops_backend.report.engine.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import com.aerotech.ced_ops_backend.common.enums.ReportStatus;
import com.aerotech.ced_ops_backend.master.module.entity.Module;
import com.aerotech.ced_ops_backend.master.module.entity.TemplateVersion;
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
 * A completed (submitted) report in the module-driven architecture. Created from
 * a fully recorded {@link ReportSession} on "Save &amp; Submit". The frozen
 * template version is preserved so the report always reflects the spec in use
 * when it was filled.
 */
@Entity
@Table(name = "report", indexes = {
        @Index(name = "idx_generic_report_module", columnList = "module_id"),
        @Index(name = "idx_generic_report_template_version", columnList = "template_version_id"),
        @Index(name = "idx_generic_report_created_by", columnList = "created_by")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedReport extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String reportNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_version_id", nullable = false)
    private TemplateVersion templateVersion;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    // ---- Immutable snapshots (Phase 4) ----
    // Historical reports stay readable even when master data changes.

    @Column(name = "module_name", nullable = false, length = 150)
    private String moduleName;

    @Column(name = "module_prefix", nullable = false, length = 10)
    private String modulePrefix;

    @Column(name = "template_version_number", nullable = false)
    private Integer templateVersionNumber;

    @Column(name = "module_type_id")
    private Long moduleTypeId;

    @Column(name = "module_type_name", nullable = false, length = 100)
    private String moduleTypeName;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "shift_name", length = 50)
    private String shiftName;

    @Column(name = "line_id")
    private Long lineId;

    @Column(name = "line_name", length = 100)
    private String lineName;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedById;

}