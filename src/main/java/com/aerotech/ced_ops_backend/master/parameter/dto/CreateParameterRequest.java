package com.aerotech.ced_ops_backend.master.parameter.dto;

import com.aerotech.ced_ops_backend.common.enums.InputType;
import com.aerotech.ced_ops_backend.common.enums.InspectionFrequency;
import com.aerotech.ced_ops_backend.common.enums.ReportType;
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

    @NotNull(message = "Report type is required")
    @Schema(description = "Report type that owns this parameter", example = "CHEMICAL_CONSUMPTION", requiredMode = Schema.RequiredMode.REQUIRED)
    private ReportType reportType;

    @NotBlank(message = "Parameter name is required")
    @Schema(description = "Name of the parameter", example = "Bath Temperature", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parameterName;

    @Schema(description = "Minimum acceptable value", example = "20.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal minValue;

    @Schema(description = "Maximum acceptable value", example = "40.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal maxValue;

    @Schema(description = "Unit of measurement", example = "°C", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String unit;

    @Schema(description = "Method used for testing", example = "Thermometer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String testMethod;

    @NotNull(message = "Frequency is required")
    @Schema(description = "Inspection frequency", example = "EVERY_SHIFT", requiredMode = Schema.RequiredMode.REQUIRED)
    private InspectionFrequency frequency;

    @NotNull(message = "Input type is required")
    @Schema(description = "Type of input expected", example = "NUMBER", requiredMode = Schema.RequiredMode.REQUIRED)
    private InputType inputType;

    @Schema(description = "Whether the parameter is mandatory", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean mandatory = true;

    @Schema(description = "Whether the parameter is visible in the report entry form", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean visible = true;

    @Schema(description = "Default value pre-filled when the parameter is rendered", example = "0.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String defaultValue;

    @NotNull(message = "Display order is required")
    @Min(1)
    @Schema(description = "Display order for sorting", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayOrder;

}
