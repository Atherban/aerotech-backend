package com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.response;

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
@Schema(description = "An individual entry within a Pre-Delivery Inspection response")
public class PreDeliveryInspectionEntryResponse {

    @Schema(description = "Unique identifier of the entry", example = "1")
    private Long id;

    @Schema(description = "ID of the inspection parameter", example = "1")
    private Long parameterId;

    @Schema(description = "Process name associated with the parameter", example = "Machining")
    private String processName;

    @Schema(description = "Name of the inspection parameter", example = "Diameter")
    private String parameterName;

    @Schema(description = "Minimum allowable value", example = "10.00")
    private BigDecimal minValue;

    @Schema(description = "Maximum allowable value", example = "15.00")
    private BigDecimal maxValue;

    @Schema(description = "Observed measurement value", example = "12.50")
    private String observedValue;

    @Schema(description = "Unit of measurement", example = "mm")
    private String unit;

    @Schema(description = "Result of the inspection")
    private InspectionResult inspectionResult;

    @Schema(description = "Remark for this entry", example = "Within tolerance")
    private String remark;

}
