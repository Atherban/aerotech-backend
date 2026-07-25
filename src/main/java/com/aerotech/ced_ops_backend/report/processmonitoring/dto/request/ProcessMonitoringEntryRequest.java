package com.aerotech.ced_ops_backend.report.processmonitoring.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "An individual entry within a Process Monitoring report")
public class ProcessMonitoringEntryRequest {

    @NotNull(message = "Parameter ID is required")
    @Schema(description = "ID of the monitoring parameter", example = "1")
    private Long parameterId;

    @NotBlank(message = "Observed value is required")
    @jakarta.validation.constraints.Size(max = 500, message = "Observed value must not exceed 500 characters")
    @Schema(description = "Observed measurement value", example = "12.5")
    private String observedValue;

    @jakarta.validation.constraints.Size(max = 1000, message = "Remark must not exceed 1000 characters")
    @Schema(description = "Remark for this entry", example = "Within specification")
    private String remark;

}