package com.aerotech.ced_ops_backend.report.engine.dto;

import com.aerotech.ced_ops_backend.common.enums.InputType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A single fillable field of a process, derived purely from the configured
 * {@code ProcessParameter}. The backend is authoritative — the frontend only
 * renders what this DTO describes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A fillable process-parameter field (backend-authoritative)")
public class ProcessParameterField {

    @Schema(description = "ProcessParameter binding ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long processParameterId;

    @Schema(description = "Global parameter ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parameterId;

    @Schema(description = "Global parameter name", example = "Temperature", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parameterName;

    @Schema(description = "Input type", example = "NUMBER", requiredMode = Schema.RequiredMode.REQUIRED)
    private InputType inputType;

    @Schema(description = "Whether the field is mandatory", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean mandatory;

    @Schema(description = "Unit of measure", example = "°C", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String unit;

    @Schema(description = "Minimum allowed value", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal minimumValue;

    @Schema(description = "Maximum allowed value", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal maximumValue;

    @Schema(description = "Default value to pre-fill", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String defaultValue;

}