package com.aerotech.ced_ops_backend.master.parameter.dto;

import com.aerotech.ced_ops_backend.common.enums.InputType;
import com.aerotech.ced_ops_backend.common.enums.InspectionFrequency;
import com.aerotech.ced_ops_backend.common.enums.ReportType;
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

    @Schema(description = "Unique ID of the parameter", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Report type that owns this parameter", example = "CHEMICAL_CONSUMPTION", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ReportType reportType;

    @Schema(description = "Name of the parameter", example = "Bath Temperature", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parameterName;

    @Schema(description = "Minimum acceptable value", example = "20.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal minValue;

    @Schema(description = "Maximum acceptable value", example = "40.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal maxValue;

    @Schema(description = "Unit of measurement", example = "°C", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String unit;

    @Schema(description = "Method used for testing", example = "Thermometer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String testMethod;

    @Schema(description = "Inspection frequency", example = "EVERY_SHIFT", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private InspectionFrequency frequency;

    @Schema(description = "Type of input expected", example = "NUMBER", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private InputType inputType;

    @Schema(description = "Whether the parameter is mandatory", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean mandatory;

    @Schema(description = "Whether the parameter is visible in the report entry form", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean visible;

    @Schema(description = "Default value pre-filled when the parameter is rendered", example = "0.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String defaultValue;

    @Schema(description = "Display order for sorting", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer displayOrder;

    @Schema(description = "Whether the parameter is active", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean active;

}
