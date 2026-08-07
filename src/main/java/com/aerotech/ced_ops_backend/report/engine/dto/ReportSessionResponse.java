package com.aerotech.ced_ops_backend.report.engine.dto;

import com.aerotech.ced_ops_backend.report.engine.enums.ReportSessionStatus;
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
@Schema(description = "Report Session (work in progress) data")
public class ReportSessionResponse {

    @Schema(description = "Session ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Module ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long moduleId;

    @Schema(description = "Module name", example = "Process Monitoring", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleName;

    @Schema(description = "Frozen template version ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long templateVersionId;

    @Schema(description = "Frozen template version number", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer versionNumber;

    @Schema(description = "ID of the current process to fill", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long currentProcessId;

    @Schema(description = "When the session started", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startedAt;

    @Schema(description = "Number of processes completed so far", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer completedProcessCount;

    @Schema(description = "Lifecycle status", example = "IN_PROGRESS", requiredMode = Schema.RequiredMode.REQUIRED)
    private ReportSessionStatus status;

}