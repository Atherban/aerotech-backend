package com.aerotech.ced_ops_backend.report.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "A single recorded value submission")
public class RecordedValueRequest {

    @NotNull(message = "processParameterId is required")
    @Schema(description = "ProcessParameter binding the value belongs to", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long processParameterId;

    @Schema(description = "Observed value for the field", example = "25.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String observedValue;

}