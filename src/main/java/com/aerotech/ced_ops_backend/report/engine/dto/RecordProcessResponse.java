package com.aerotech.ced_ops_backend.report.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response to Save &amp; Next / Save &amp; Submit. Carries the next process step
 * for the frontend to render (null when the report is complete).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of recording a process")
public class RecordProcessResponse {

    @Schema(description = "Session ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @Schema(description = "Total completed process count after this save", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer completedProcessCount;

    @Schema(description = "Next process for the frontend to render, or null when the report is done", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ReportProcessStep nextProcess;

    @Schema(description = "Whether saving this process submitted the completed report", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean reportCompleted;

    @Schema(description = "Completed report reference (when reportCompleted is true)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private CompletedReportResponse report;

}