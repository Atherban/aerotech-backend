package com.aerotech.ced_ops_backend.report.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to record (save) the current process values")
public class RecordProcessRequest {

    @NotEmpty(message = "At least one recorded value is expected")
    @Schema(description = "Values for every fillable field of the current process", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RecordedValueRequest> values;

}