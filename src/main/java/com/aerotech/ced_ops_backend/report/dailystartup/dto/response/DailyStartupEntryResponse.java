package com.aerotech.ced_ops_backend.report.dailystartup.dto.response;

import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "An individual entry within a Daily Startup response")
public class DailyStartupEntryResponse {

    @Schema(description = "Unique identifier of the entry", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "ID of the startup parameter", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long parameterId;

    @Schema(description = "Name of the startup parameter", example = "Air Pressure", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parameterName;

    @Schema(description = "Minimum allowable value", example = "4.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal minValue;

    @Schema(description = "Maximum allowable value", example = "8.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal maxValue;

    @Schema(description = "Observed startup value", example = "OK", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String observedValue;

    @Schema(description = "Unit of measurement", example = "bar", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String unit;

    @Schema(description = "Result of the check", example = "PASS", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private InspectionResult inspectionResult;

    @Schema(description = "Remark for this entry", example = "Machine ready", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remark;

}
