package com.aerotech.ced_ops_backend.report.predeliveryinspection.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseReport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pre_delivery_inspection_reports", indexes = {
    @Index(name = "idx_pdi_rep_status", columnList = "status"),
    @Index(name = "idx_pdi_rep_report_date", columnList = "report_date"),
    @Index(name = "idx_pdi_rep_shift_id", columnList = "shift_id"),
    @Index(name = "idx_pdi_rep_line_id", columnList = "line_id"),
    @Index(name = "idx_pdi_rep_created_by", columnList = "created_by"),
    @Index(name = "idx_pdi_rep_approved_by", columnList = "approved_by")
})
@Getter
@Setter
@NoArgsConstructor
public class PreDeliveryInspectionReport extends BaseReport {

    @Column(name = "product_part_number", length = 150)
    private String productPartNumber;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "inspector_name", length = 100)
    private String inspectorName;

}
