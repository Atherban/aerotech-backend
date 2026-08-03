package com.aerotech.ced_ops_backend.report.dailystartup.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "An individual entry within a Daily Startup report")
public class DailyStartupEntryRequest {

    @NotNull(message = "Parameter ID is required")
    @Schema(description = "ID of the startup parameter", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parameterId;

    @NotBlank(message = "Observed value is required")
    @jakarta.validation.constraints.Size(max = 500, message = "Observed value must not exceed 500 characters")
    @Schema(description = "Observed startup value", example = "OK", requiredMode = Schema.RequiredMode.REQUIRED)
    private String observedValue;

    @jakarta.validation.constraints.Size(max = 1000, message = "Remark must not exceed 1000 characters")
    @Schema(description = "Remark for this entry", example = "Machine ready", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remark;

}
