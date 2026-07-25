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

    @Schema(description = "Unique identifier of the report", example = "1")
    private Long id;

    @Schema(description = "Auto-generated report number", example = "FPI-2025-0001")
    private String reportNumber;

    @Schema(description = "Type of report", example = "FIRST_PIECE_INSPECTION")
    private String reportType;

    @Schema(description = "Date of the report", example = "2025-01-15")
    private LocalDate reportDate;

    @Schema(description = "Status of the report", example = "DRAFT")
    private String status;

    @Schema(description = "Shift name", example = "Morning")
    private String shiftName;

    @Schema(description = "Production line name", example = "Line A")
    private String lineName;

    @Schema(description = "Employee who created the report", example = "jdoe")
    private String createdBy;

    @Schema(description = "Timestamp when the report was created", example = "2025-01-15T08:00:00")
    private LocalDateTime createdAt;

}
