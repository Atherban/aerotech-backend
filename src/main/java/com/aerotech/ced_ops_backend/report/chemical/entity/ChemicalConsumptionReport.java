package com.aerotech.ced_ops_backend.report.chemical.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseReport;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chemical_consumption_reports", indexes = {
    @Index(name = "idx_chem_rep_status", columnList = "status"),
    @Index(name = "idx_chem_rep_report_date", columnList = "report_date"),
    @Index(name = "idx_chem_rep_shift_id", columnList = "shift_id"),
    @Index(name = "idx_chem_rep_line_id", columnList = "line_id"),
    @Index(name = "idx_chem_rep_created_by", columnList = "created_by"),
    @Index(name = "idx_chem_rep_approved_by", columnList = "approved_by")
})
@Getter
@Setter
@NoArgsConstructor
public class ChemicalConsumptionReport extends BaseReport {

}
