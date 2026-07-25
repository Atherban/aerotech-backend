package com.aerotech.ced_ops_backend.report.globalsearch.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single search result item from global search")
public class GlobalSearchResultItem {

    @Schema(description = "Unique identifier of the report", example = "1")
    private Long id;

    @Schema(description = "Auto-generated report number", example = "FPI-2025-0001")
    private String reportNumber;

    @Schema(description = "Type of report", example = "FIRST_PIECE_INSPECTION")
    private String reportType;

    @Schema(description = "Date of the report", example = "2025-01-15")
    private LocalDate reportDate;

    @Schema(description = "Shift name", example = "Morning")
    private String shiftName;

    @Schema(description = "Production line name", example = "Line A")
    private String lineName;

    @Schema(description = "Status of the report", example = "APPROVED")
    private String status;

    @Schema(description = "Employee who created the report", example = "jdoe")
    private String createdBy;

    @Schema(description = "Employee who approved the report", example = "asmith")
    private String approvedBy;

    @Schema(description = "Summary of the report content", example = "First piece inspection for casting CAST-001")
    private String summary;

}
