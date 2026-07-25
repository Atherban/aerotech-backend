package com.aerotech.ced_ops_backend.master.parameter.dto;

import com.aerotech.ced_ops_backend.common.enums.InputType;
import com.aerotech.ced_ops_backend.common.enums.InspectionFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new inspection parameter")
public class CreateParameterRequest {

    @NotNull(message = "Process is required")
    @Schema(description = "ID of the associated process", example = "1")
    private Long processId;

    @NotBlank(message = "Parameter name is required")
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

    @NotNull(message = "Frequency is required")
    @Schema(description = "Inspection frequency", example = "EACH_HOUR")
    private InspectionFrequency frequency;

    @NotNull(message = "Input type is required")
    @Schema(description = "Type of input expected", example = "NUMERIC")
    private InputType inputType;

    @Schema(description = "Whether the parameter is mandatory", example = "true")
    private Boolean mandatory = true;

    @NotNull(message = "Display order is required")
    @Min(1)
    @Schema(description = "Display order for sorting", example = "1")
    private Integer displayOrder;

}