package com.aerotech.ced_ops_backend.report.firstpieceinspection.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "first_piece_inspection_entries", indexes = {
    @Index(name = "idx_fpi_entries_report_id", columnList = "report_id"),
    @Index(name = "idx_fpi_entries_parameter_id", columnList = "parameter_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirstPieceInspectionEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private FirstPieceInspectionReport report;

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
