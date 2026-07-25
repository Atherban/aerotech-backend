package com.aerotech.ced_ops_backend.master.parameter.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import com.aerotech.ced_ops_backend.common.enums.InputType;
import com.aerotech.ced_ops_backend.common.enums.InspectionFrequency;
import com.aerotech.ced_ops_backend.master.process.entity.ProcessMaster;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "parameter_master", indexes = {
    @Index(name = "idx_parameter_master_process_id", columnList = "process_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterMaster extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private ProcessMaster process;

    @Column(nullable = false, length = 150)
    private String parameterName;

    @Column(precision = 10, scale = 2)
    private BigDecimal minValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxValue;

    @Column(length = 30)
    private String unit;

    @Column(length = 150)
    private String testMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InputType inputType;

    @Builder.Default
    @Column(nullable = false)
    private Boolean mandatory = true;

    @Column(nullable = false)
    private Integer displayOrder;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

}