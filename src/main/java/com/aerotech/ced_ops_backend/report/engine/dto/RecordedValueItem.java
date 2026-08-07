package com.aerotech.ced_ops_backend.report.engine.dto;

import com.aerotech.ced_ops_backend.common.enums.InputType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A recorded value, grouped under its process")
public class RecordedValueItem {

    @Schema(description = "Recorded value ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "ProcessParameter binding ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long processParameterId;

    @Schema(description = "Parameter ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parameterId;

    @Schema(description = "Parameter name", example = "Temperature", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parameterName;

    @Schema(description = "Input type", example = "NUMBER", requiredMode = Schema.RequiredMode.REQUIRED)
    private InputType inputType;

    @Schema(description = "Observed value", example = "25.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String observedValue;

}