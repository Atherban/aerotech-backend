package com.aerotech.ced_ops_backend.report.support;

import com.aerotech.ced_ops_backend.common.enums.ReportStatus;
import com.aerotech.ced_ops_backend.common.pagination.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Shared filter for paginated report listing, used by every report module.
 * Mirrors the fields already supported by {@code GlobalSearchRequest} so all
 * report modules get the same optional query-param surface.
 */
@Getter
@Setter
@Schema(description = "Filter for paginated report listing (shared across all report types)")
public class ReportFilterRequest extends PageRequest {

    @Schema(description = "Partial or full report number", example = "PMR-2026", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportNumber;

    @Schema(description = "Report status filter", example = "SUBMITTED", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ReportStatus status;

    @Schema(description = "Shift ID filter", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long shiftId;

    @Schema(description = "Line ID filter", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long lineId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Earliest report date (ISO yyyy-MM-dd)", example = "2026-01-01", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Latest report date (ISO yyyy-MM-dd)", example = "2026-12-31", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate dateTo;

    @Schema(description = "Filter by approval status", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean approved;

    @Override
    public boolean hasSearchCriteria() {
        return super.hasSearchCriteria()
                || PageRequest.isPresent(reportNumber)
                || status != null
                || shiftId != null
                || lineId != null
                || dateFrom != null
                || dateTo != null
                || approved != null;
    }

}
