package com.aerotech.ced_ops_backend.master.parameter.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import com.aerotech.ced_ops_backend.common.enums.InputType;
import com.aerotech.ced_ops_backend.common.enums.InspectionFrequency;
import com.aerotech.ced_ops_backend.common.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "parameter_master", indexes = {
    @Index(name = "idx_parameter_master_report_type", columnList = "report_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterMaster extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

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

    @Builder.Default
    @Column(nullable = false)
    private Boolean visible = true;

    @Column(length = 255)
    private String defaultValue;

    @Column(nullable = false)
    private Integer displayOrder;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

}
