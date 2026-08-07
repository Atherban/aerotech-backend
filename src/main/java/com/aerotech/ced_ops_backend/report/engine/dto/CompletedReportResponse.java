package com.aerotech.ced_ops_backend.report.engine.dto;

import com.aerotech.ced_ops_backend.common.enums.ReportStatus;
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
@Schema(description = "Completed (submitted) report data")
public class CompletedReportResponse {

    @Schema(description = "Report ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Generated report number", example = "PMR-20260807-00001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportNumber;

    @Schema(description = "Module name", example = "Process Monitoring", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleName;

    @Schema(description = "Frozen template version number", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer versionNumber;

    @Schema(description = "Module prefix", example = "PMR", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prefix;

    @Schema(description = "When the report session started", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startedAt;

    @Schema(description = "When the report was submitted", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime submittedAt;

    @Schema(description = "Report status", example = "SUBMITTED", requiredMode = Schema.RequiredMode.REQUIRED)
    private ReportStatus status;

    @Schema(description = "Originating session ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

}