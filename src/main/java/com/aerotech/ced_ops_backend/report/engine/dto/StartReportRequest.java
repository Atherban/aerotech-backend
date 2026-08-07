package com.aerotech.ced_ops_backend.report.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to start a new report session for a module")
public class StartReportRequest {

    @NotNull(message = "moduleId is required")
    @Schema(description = "ID of the module to build the report from", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long moduleId;

    @Schema(description = "Optional shift the report belongs to (snapshotted on the completed report)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long shiftId;

    @Schema(description = "Optional production line the report belongs to (snapshotted on the completed report)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long lineId;

}