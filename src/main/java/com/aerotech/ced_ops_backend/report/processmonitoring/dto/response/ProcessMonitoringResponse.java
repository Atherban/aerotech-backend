package com.aerotech.ced_ops_backend.report.processmonitoring.dto.response;

import com.aerotech.ced_ops_backend.common.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload for a Process Monitoring report")
public class ProcessMonitoringResponse {

    @Schema(description = "Unique identifier of the report", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Auto-generated report number", example = "PM-2025-0001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportNumber;

    @Schema(description = "Date of the monitoring", example = "2025-01-15", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate reportDate;

    @Schema(description = "Shift name", example = "Morning", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String shift;

    @Schema(description = "Production line name", example = "Line A", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String line;

    @Schema(description = "Employee who created the report", example = "jdoe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String createdBy;

    @Schema(description = "Employee who approved the report", example = "asmith", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String approvedBy;

    @Schema(description = "Current status of the report", example = "APPROVED", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ReportStatus status;

    @Schema(description = "Additional remarks", example = "All processes running normally", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remarks;

    @Schema(description = "Timestamp when the report was approved", example = "2025-01-15T10:30:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime approvedAt;

    @Schema(description = "Timestamp when the report was created", example = "2025-01-15T08:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime createdAt;

    @Schema(description = "List of monitoring entries", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ProcessMonitoringEntryResponse> entries;

}
