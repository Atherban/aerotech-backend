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
@Schema(description = "Request body for updating an existing inspection parameter")
public class UpdateParameterRequest {

    @NotBlank(message = "Parameter name is required")
    @Schema(description = "Name of the parameter", example = "Temperature", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parameterName;

    @Schema(description = "Minimum acceptable value", example = "20.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal minValue;

    @Schema(description = "Maximum acceptable value", example = "100.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal maxValue;

    @Schema(description = "Unit of measurement", example = "°C", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String unit;

    @Schema(description = "Method used for testing", example = "Visual Inspection", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String testMethod;

    @NotNull(message = "Frequency is required")
    @Schema(description = "Inspection frequency", example = "HOURLY", requiredMode = Schema.RequiredMode.REQUIRED)
    private InspectionFrequency frequency;

    @NotNull(message = "Input type is required")
    @Schema(description = "Type of input expected", example = "NUMBER", requiredMode = Schema.RequiredMode.REQUIRED)
    private InputType inputType;

    @Schema(description = "Whether the parameter is mandatory", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean mandatory;

    @Schema(description = "Whether the parameter is visible in the report entry form", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean visible;

    @Schema(description = "Default value pre-filled when the parameter is rendered", example = "0.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String defaultValue;

    @NotNull(message = "Display order is required")
    @Min(1)
    @Schema(description = "Display order for sorting", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayOrder;

    @Schema(description = "Whether the parameter is active", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean active;

}
