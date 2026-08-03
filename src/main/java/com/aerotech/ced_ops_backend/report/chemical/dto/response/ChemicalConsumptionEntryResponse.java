package com.aerotech.ced_ops_backend.report.chemical.dto.response;

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
@Schema(description = "An individual entry within a Chemical Consumption response")
public class ChemicalConsumptionEntryResponse {

    @Schema(description = "Unique identifier of the entry", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "ID of the consumption parameter", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long parameterId;

    @Schema(description = "Name of the consumption parameter", example = "Bath Concentration", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parameterName;

    @Schema(description = "Minimum allowable value", example = "10.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal minValue;

    @Schema(description = "Maximum allowable value", example = "15.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal maxValue;

    @Schema(description = "Observed consumption value", example = "12.50", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String observedValue;

    @Schema(description = "Unit of measurement", example = "g/L", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String unit;

    @Schema(description = "Result of the inspection", example = "PASS", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private InspectionResult inspectionResult;

    @Schema(description = "Remark for this entry", example = "Within tolerance", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remark;

}
