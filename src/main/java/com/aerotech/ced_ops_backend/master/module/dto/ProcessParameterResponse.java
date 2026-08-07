package com.aerotech.ced_ops_backend.master.module.dto;

import com.aerotech.ced_ops_backend.common.enums.InputType;
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
@Schema(description = "ProcessParameter binding data")
public class ProcessParameterResponse {

    @Schema(description = "ProcessParameter ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Owning process ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long processId;

    @Schema(description = "Bound global parameter reference", requiredMode = Schema.RequiredMode.REQUIRED)
    private ParameterSummaryResponse parameter;

    @Schema(description = "The ONLY ordering mechanism", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayOrder;

    @Schema(description = "Whether the parameter is mandatory", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean mandatory;

    @Schema(description = "Whether the parameter is visible", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean visible;

    @Schema(description = "Optional default value", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String defaultValue;

    @Schema(description = "Optional unit of measure", example = "°C", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String unit;

    @Schema(description = "Optional minimum value", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal minimumValue;

    @Schema(description = "Optional maximum value", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal maximumValue;

    @Schema(description = "Whether the binding is active", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean active;

    @Schema(description = "Input type derived from the bound global parameter", example = "NUMBER", requiredMode = Schema.RequiredMode.REQUIRED)
    private InputType inputType;

}