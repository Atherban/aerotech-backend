package com.aerotech.ced_ops_backend.report.dailyinspection.dto.response;

import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyInspectionEntryResponse {

    private Long id;

    private Long parameterId;

    private String processName;

    private String parameterName;

    private BigDecimal minValue;

    private BigDecimal maxValue;

    private String observedValue;

    private String unit;

    private InspectionResult inspectionResult;

    private String remark;

}
