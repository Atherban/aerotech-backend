package com.aerotech.ced_ops_backend.master.module.dto;

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
@Schema(description = "Module Type data")
public class ModuleTypeResponse {

    @Schema(description = "Module type ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Module type name", example = "Quality", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Optional description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "Whether the module type is active", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean active;

}