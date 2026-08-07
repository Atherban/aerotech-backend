package com.aerotech.ced_ops_backend.master.module.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "process_parameter", indexes = {
        @Index(name = "idx_process_parameter_process", columnList = "process_id"),
        @Index(name = "idx_process_parameter_parameter", columnList = "parameter_id")
}, uniqueConstraints = @UniqueConstraint(name = "uk_process_parameter",
        columnNames = {"process_id", "parameter_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessParameter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id", nullable = false)
    private Process process;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parameter_id", nullable = false)
    private Parameter parameter;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder.Default
    @Column(nullable = false)
    private Boolean mandatory = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean visible = true;

    @Column(name = "default_value", length = 255)
    private String defaultValue;

    @Column(length = 30)
    private String unit;

    @Column(name = "minimum_value", precision = 10, scale = 2)
    private BigDecimal minimumValue;

    @Column(name = "maximum_value", precision = 10, scale = 2)
    private BigDecimal maximumValue;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

}