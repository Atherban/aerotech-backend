package com.aerotech.ced_ops_backend.master.module.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Request to create a ProcessParameter binding")
public class CreateProcessParameterRequest {

    @NotNull(message = "parameterId is required")
    @Schema(description = "ID of the global parameter to bind", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parameterId;

    @NotNull(message = "displayOrder is required")
    @Min(1)
    @Schema(description = "The ONLY ordering mechanism", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayOrder;

    @Schema(description = "Whether the parameter is mandatory", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean mandatory;

    @Schema(description = "Whether the parameter is visible", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean visible;

    @Size(max = 255, message = "defaultValue must not exceed 255 characters")
    @Schema(description = "Optional default value", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String defaultValue;

    @Size(max = 30, message = "unit must not exceed 30 characters")
    @Schema(description = "Optional unit of measure", example = "°C", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String unit;

    @Schema(description = "Optional minimum value (numeric parameters)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal minimumValue;

    @Schema(description = "Optional maximum value (numeric parameters)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal maximumValue;

}