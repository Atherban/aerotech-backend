package com.aerotech.ced_ops_backend.master.parameter.dto;

import com.aerotech.ced_ops_backend.common.enums.InputType;
import com.aerotech.ced_ops_backend.common.enums.InspectionFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response body for inspection parameter data")
public class ParameterResponse {

    @Schema(description = "Unique ID of the parameter", example = "1")
    private Long id;

    @Schema(description = "ID of the associated process", example = "1")
    private Long processId;

    @Schema(description = "Name of the associated process", example = "Painting")
    private String processName;

    @Schema(description = "Name of the parameter", example = "Temperature")
    private String parameterName;

    @Schema(description = "Minimum acceptable value", example = "20.0")
    private BigDecimal minValue;

    @Schema(description = "Maximum acceptable value", example = "100.0")
    private BigDecimal maxValue;

    @Schema(description = "Unit of measurement", example = "°C")
    private String unit;

    @Schema(description = "Method used for testing", example = "Visual Inspection")
    private String testMethod;

    @Schema(description = "Inspection frequency", example = "EACH_HOUR")
    private InspectionFrequency frequency;

    @Schema(description = "Type of input expected", example = "NUMERIC")
    private InputType inputType;

    @Schema(description = "Whether the parameter is mandatory", example = "true")
    private Boolean mandatory;

    @Schema(description = "Display order for sorting", example = "1")
    private Integer displayOrder;

    @Schema(description = "Whether the parameter is active", example = "true")
    private Boolean active;

}