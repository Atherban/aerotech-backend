package com.aerotech.ced_ops_backend.report.processmonitoring.dto.response;

import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "An individual entry within a Process Monitoring response")
public class ProcessMonitoringEntryResponse {

    @Schema(description = "Unique identifier of the entry", example = "1")
    private Long id;

    @Schema(description = "ID of the monitoring parameter", example = "1")
    private Long parameterId;

    @Schema(description = "Process name associated with the parameter", example = "Machining")
    private String processName;

    @Schema(description = "Name of the monitoring parameter", example = "Temperature")
    private String parameterName;

    @Schema(description = "Minimum allowable value", example = "100.00")
    private BigDecimal minValue;

    @Schema(description = "Maximum allowable value", example = "200.00")
    private BigDecimal maxValue;

    @Schema(description = "Observed measurement value", example = "150.00")
    private String observedValue;

    @Schema(description = "Unit of measurement", example = "°C")
    private String unit;

    @Schema(description = "Result of the inspection")
    private InspectionResult inspectionResult;

    @Schema(description = "Remark for this entry", example = "Within specification")
    private String remark;

}