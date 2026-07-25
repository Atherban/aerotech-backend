package com.aerotech.ced_ops_backend.report.processmonitoring.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "process_monitoring_entries", indexes = {
    @Index(name = "idx_pm_entries_report_id", columnList = "report_id"),
    @Index(name = "idx_pm_entries_parameter_id", columnList = "parameter_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessMonitoringEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private ProcessMonitoringReport report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parameter_id", nullable = false)
    private ParameterMaster parameter;

    @Column(nullable = false, length = 200)
    private String observedValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionResult inspectionResult;

    @Column(length = 500)
    private String remark;

}