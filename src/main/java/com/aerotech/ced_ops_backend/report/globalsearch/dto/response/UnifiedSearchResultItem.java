package com.aerotech.ced_ops_backend.report.globalsearch.dto.response;

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
@Schema(description = "A single unified search result (report, user, or parameter)")
public class UnifiedSearchResultItem {

    @Schema(description = "Entity type of the result", example = "REPORT",
            allowableValues = {"REPORT", "USER", "PARAMETER"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(description = "Unique identifier of the entity", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Primary label (report number / employee ID / parameter name)", example = "PMR-20260802-00001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String title;

    @Schema(description = "Secondary label (report type, user full name, or parameter unit)", example = "PROCESS_MONITORING", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String subtitle;

    @Schema(description = "Report type (reports and parameters)", example = "PROCESS_MONITORING", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportType;

    @Schema(description = "Report status (reports only)", example = "APPROVED", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String status;

    @Schema(description = "Shift name (reports only)", example = "Morning", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String shiftName;

    @Schema(description = "Production line name (reports only)", example = "Line A", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String lineName;

    @Schema(description = "Employee who created the report or the user's full name", example = "Jane Smith", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String actor;

    @Schema(description = "Report date (reports only)", example = "2026-08-02", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate reportDate;

    @Schema(description = "Timestamp when the entity was created", example = "2026-08-02T08:15:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime createdAt;

}
