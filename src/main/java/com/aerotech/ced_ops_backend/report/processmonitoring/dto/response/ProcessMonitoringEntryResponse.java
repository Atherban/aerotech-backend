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

    @Schema(description = "Unique identifier of the entry", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "ID of the monitoring parameter", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long parameterId;

    @Schema(description = "Name of the monitoring parameter", example = "Temperature", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parameterName;

    @Schema(description = "Minimum allowable value", example = "100.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal minValue;

    @Schema(description = "Maximum allowable value", example = "200.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal maxValue;

    @Schema(description = "Observed measurement value", example = "150.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String observedValue;

    @Schema(description = "Unit of measurement", example = "°C", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String unit;

    @Schema(description = "Result of the inspection", example = "PASS", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private InspectionResult inspectionResult;

    @Schema(description = "Remark for this entry", example = "Within specification", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remark;

}
