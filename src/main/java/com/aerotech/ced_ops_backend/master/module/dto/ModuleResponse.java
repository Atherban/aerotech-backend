package com.aerotech.ced_ops_backend.master.module.dto;

import com.aerotech.ced_ops_backend.master.module.enums.ModuleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Module (reusable report template) data")
public class ModuleResponse {

    @Schema(description = "Module ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Owning module type", requiredMode = Schema.RequiredMode.REQUIRED)
    private ModuleTypeSummaryResponse moduleType;

    @Schema(description = "Module name", example = "Process Monitoring", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Unique report-number prefix", example = "PMR", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prefix;

    @Schema(description = "Optional description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "Lifecycle status", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private ModuleStatus status;

    @Schema(description = "Details of the latest ACTIVE template version", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private TemplateVersionResponse latestActiveVersion;

}