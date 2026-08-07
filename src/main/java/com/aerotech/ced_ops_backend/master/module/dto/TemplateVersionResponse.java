package com.aerotech.ced_ops_backend.master.module.dto;

import com.aerotech.ced_ops_backend.master.module.enums.TemplateVersionStatus;
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
@Schema(description = "Template version data")
public class TemplateVersionResponse {

    @Schema(description = "Template version ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Owning module ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long moduleId;

    @Schema(description = "Version number", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer versionNumber;

    @Schema(description = "Lifecycle status", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private TemplateVersionStatus status;

    @Schema(description = "Note describing what changed in this version", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String changeNote;

}