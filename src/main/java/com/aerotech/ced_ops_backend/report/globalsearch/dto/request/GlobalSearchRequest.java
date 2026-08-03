package com.aerotech.ced_ops_backend.report.globalsearch.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Request payload for global search across reports")
public class GlobalSearchRequest {

    @Size(max = 100, message = "Report number must not exceed 100 characters")
    @Parameter(description = "Partial or full report number")
    @Schema(description = "Partial or full report number", example = "FPI-2025", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportNumber;

    @Size(max = 50, message = "Report type must not exceed 50 characters")
    @Parameter(description = "Report type filter")
    @Schema(description = "Report type filter", example = "FIRST_PIECE_INSPECTION", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportType;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    @Parameter(description = "Report status filter")
    @Schema(description = "Report status filter", example = "DRAFT", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String status;

    @Size(max = 200, message = "Employee name must not exceed 200 characters")
    @Parameter(description = "Employee name (partial match)")
    @Schema(description = "Employee name (partial match)", example = "John", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String employeeName;

    @Size(max = 50, message = "Employee ID must not exceed 50 characters")
    @Parameter(description = "Employee ID (exact match)")
    @Schema(description = "Employee ID (exact match)", example = "EMP001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String employeeId;

    @Parameter(description = "Shift ID")
    @Schema(description = "Shift ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long shiftId;

    @Parameter(description = "Line ID")
    @Schema(description = "Line ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long lineId;

    @Parameter(description = "Start date (ISO format: yyyy-MM-dd)")
    @Schema(description = "Start date (ISO format: yyyy-MM-dd)", example = "2025-01-01", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @Parameter(description = "End date (ISO format: yyyy-MM-dd)")
    @Schema(description = "End date (ISO format: yyyy-MM-dd)", example = "2025-12-31", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    @Parameter(description = "Partial match on remarks")
    @Schema(description = "Partial match on remarks", example = "urgent", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remarks;

    @Parameter(description = "Filter by approval status: true=approved, false=non-approved")
    @Schema(description = "Filter by approval status", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean approved;

    @Size(max = 500, message = "Keyword must not exceed 500 characters")
    @Parameter(description = "Keyword search across report number, remarks, and employee name")
    @Schema(description = "Keyword search across report number, remarks, and employee name", example = "urgent", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String keyword;

    @Size(max = 50, message = "Creator ID must not exceed 50 characters")
    @Parameter(description = "Creator employee ID (exact match)")
    @Schema(description = "Creator employee ID (exact match)", example = "jdoe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String createdBy;

    @Size(max = 50, message = "Approver ID must not exceed 50 characters")
    @Parameter(description = "Approver employee ID (exact match)")
    @Schema(description = "Approver employee ID (exact match)", example = "asmith", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String approvedBy;

    @Size(max = 50, message = "Sort field must not exceed 50 characters")
    @Parameter(description = "Sort field: reportDate, reportNumber, createdAt, updatedAt, status")
    @Schema(description = "Sort field", example = "reportDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sortBy;

    @Size(max = 4, message = "Sort direction must be ASC or DESC")
    @Parameter(description = "Sort direction: ASC or DESC")
    @Schema(description = "Sort direction", example = "DESC", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sortDirection;

    @Min(value = 0, message = "Page number must be non-negative")
    @Parameter(description = "Page number (zero-based)")
    @Schema(description = "Page number (zero-based)", example = "0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Parameter(description = "Page size")
    @Schema(description = "Page size", example = "20", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int size = 20;

}
