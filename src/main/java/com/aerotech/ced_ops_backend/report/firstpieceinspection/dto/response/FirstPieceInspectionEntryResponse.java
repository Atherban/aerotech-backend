package com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.response;

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
@Schema(description = "An individual entry within a First Piece Inspection response")
public class FirstPieceInspectionEntryResponse {

    @Schema(description = "Unique identifier of the entry", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "ID of the inspection parameter", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long parameterId;

    @Schema(description = "Name of the inspection parameter", example = "Diameter", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parameterName;

    @Schema(description = "Minimum allowable value", example = "10.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal minValue;

    @Schema(description = "Maximum allowable value", example = "15.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal maxValue;

    @Schema(description = "Observed measurement value", example = "12.50", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String observedValue;

    @Schema(description = "Unit of measurement", example = "mm", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String unit;

    @Schema(description = "Result of the inspection", example = "PASS", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private InspectionResult inspectionResult;

    @Schema(description = "Remark for this entry", example = "Within tolerance", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remark;

}
