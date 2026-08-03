package com.aerotech.ced_ops_backend.report.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A recent report lifecycle event for the dashboard activity feed")
public class RecentActivityResponse {

    @Schema(description = "Unique identifier of the report", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Auto-generated report number", example = "PMR-20260802-00001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportNumber;

    @Schema(description = "Type of report", example = "PROCESS_MONITORING", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportType;

    @Schema(description = "Lifecycle action that occurred", example = "APPROVED", allowableValues = {"CREATED", "APPROVED", "REJECTED"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String action;

    @Schema(description = "Current status of the report", example = "APPROVED", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String status;

    @Schema(description = "User who performed the action", example = "Jane Smith", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String actor;

    @Schema(description = "Timestamp of the lifecycle event", example = "2026-08-02T08:15:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime timestamp;

}
