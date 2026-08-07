package com.aerotech.ced_ops_backend.report.engine.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import com.aerotech.ced_ops_backend.master.module.entity.Parameter;
import com.aerotech.ced_ops_backend.master.module.entity.ProcessParameter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;

/**
 * A recorded value, always grouped under its {@link RecordedProcess}.
 */
@Entity
@Table(name = "recorded_value", indexes = {
        @Index(name = "idx_recorded_value_recorded_process", columnList = "recorded_process_id"),
        @Index(name = "idx_recorded_value_process_parameter", columnList = "process_parameter_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordedValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_process_id", nullable = false)
    private RecordedProcess recordedProcess;

    /**
     * The exact process parameter specification this value belongs to
     * (references processId + parameterId). Per architectural decision 9,
     * RecordedValue references reportId/processId/parameterId/processParameterId
     * — here reportId is carried transitively via the recorded process' session.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_parameter_id", nullable = false)
    private ProcessParameter processParameter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parameter_id", nullable = false)
    private Parameter parameter;

    @Column(name = "observed_value", length = 1000)
    private String observedValue;

    // ---- Immutable snapshots (Phase 4) ----
    // Spec in use when the value was saved; analytics derive PASS/FAIL
    // from minimum_value/maximum_value so later spec edits never change
    // historical reports.

    @Column(name = "parameter_name", nullable = false, length = 150)
    private String parameterName;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "input_type", nullable = false, length = 30)
    private String inputType;

    @Column(name = "minimum_value", precision = 10, scale = 2)
    private BigDecimal minimumValue;

    @Column(name = "maximum_value", precision = 10, scale = 2)
    private BigDecimal maximumValue;

}