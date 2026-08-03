package com.aerotech.ced_ops_backend.report.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A recently created report summary for the dashboard")
public class RecentReportResponse {

    @Schema(description = "Unique identifier of the report", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Auto-generated report number", example = "FPI-2025-0001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportNumber;

    @Schema(description = "Type of report", example = "FIRST_PIECE_INSPECTION", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportType;

    @Schema(description = "Date of the report", example = "2025-01-15", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate reportDate;

    @Schema(description = "Status of the report", example = "DRAFT", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String status;

    @Schema(description = "Shift name", example = "Morning", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String shiftName;

    @Schema(description = "Production line name", example = "Line A", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String lineName;

    @Schema(description = "Employee who created the report", example = "jdoe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String createdBy;

    @Schema(description = "Timestamp when the report was created", example = "2025-01-15T08:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime createdAt;

}
