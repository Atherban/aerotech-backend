package com.aerotech.ced_ops_backend.report.processmonitoring.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a Process Monitoring report")
public class CreateProcessMonitoringRequest {

    @NotNull(message = "Report date is required")
    @Schema(description = "Date of the monitoring report", example = "2025-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate reportDate;

    @Schema(description = "ID of the shift. Omitted to auto-detect from current time", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long shiftId;

    @NotNull(message = "Line ID is required")
    @Schema(description = "ID of the production line", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long lineId;

    @jakarta.validation.constraints.Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    @Schema(description = "Additional remarks", example = "All processes running normally", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remarks;

    @Valid
    @NotEmpty(message = "At least one entry is required")
    @Schema(description = "List of monitoring entries", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ProcessMonitoringEntryRequest> entries;

}
