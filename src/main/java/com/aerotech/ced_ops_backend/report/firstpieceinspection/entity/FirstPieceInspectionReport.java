package com.aerotech.ced_ops_backend.report.firstpieceinspection.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseReport;
import com.aerotech.ced_ops_backend.master.process.entity.ProcessMaster;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "first_piece_inspection_reports", indexes = {
    @Index(name = "idx_fpi_rep_status", columnList = "status"),
    @Index(name = "idx_fpi_rep_report_date", columnList = "report_date"),
    @Index(name = "idx_fpi_rep_shift_id", columnList = "shift_id"),
    @Index(name = "idx_fpi_rep_line_id", columnList = "line_id"),
    @Index(name = "idx_fpi_rep_created_by", columnList = "created_by"),
    @Index(name = "idx_fpi_rep_approved_by", columnList = "approved_by"),
    @Index(name = "idx_fpi_rep_process_id", columnList = "process_id")
})
@Getter
@Setter
@NoArgsConstructor
public class FirstPieceInspectionReport extends BaseReport {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private ProcessMaster process;

    @Column(name = "product_casting_number", length = 100)
    private String productCastingNumber;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    @Column(name = "inspector_name", length = 100)
    private String inspectorName;

}
