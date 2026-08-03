package com.aerotech.ced_ops_backend.report.globalsearch.dto.request;

import com.aerotech.ced_ops_backend.common.pagination.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Unified enterprise search request spanning reports, users and parameters.
 * Builds on the shared {@link PageRequest} pagination contract.
 */
@Getter
@Setter
@Schema(description = "Unified enterprise search request across reports, users and parameters")
public class UnifiedSearchRequest extends PageRequest {

    public static final String TYPE_REPORT = "REPORT";
    public static final String TYPE_USER = "USER";
    public static final String TYPE_PARAMETER = "PARAMETER";

    @Schema(description = "Restrict results to one entity type", example = "REPORT",
            allowableValues = {"REPORT", "USER", "PARAMETER"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String type;

    @Schema(description = "Partial or full report number / parameter name / employee ID", example = "PMR", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportNumber;

    @Schema(description = "Report type filter", example = "PROCESS_MONITORING", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportType;

    @Schema(description = "Report status filter", example = "APPROVED", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String status;

    @Schema(description = "Employee name filter (creator on reports, full name on users)", example = "John", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String employeeName;

    @Schema(description = "User role filter (users only)", example = "OPERATOR", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String role;

    @Schema(description = "Shift ID filter (reports only)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long shiftId;

    @Schema(description = "Line ID filter (reports only)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long lineId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Earliest report date (ISO yyyy-MM-dd)", example = "2026-01-01", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Latest report date (ISO yyyy-MM-dd)", example = "2026-12-31", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate dateTo;

    @Override
    public boolean hasSearchCriteria() {
        return super.hasSearchCriteria()
                || PageRequest.isPresent(type)
                || PageRequest.isPresent(reportNumber)
                || PageRequest.isPresent(reportType)
                || PageRequest.isPresent(status)
                || PageRequest.isPresent(employeeName)
                || PageRequest.isPresent(role)
                || shiftId != null
                || lineId != null
                || dateFrom != null
                || dateTo != null;
    }

}
