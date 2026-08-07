package com.aerotech.ced_ops_backend.master.module.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Request to update Module header metadata")
public class UpdateModuleRequest {

    @Size(max = 150, message = "Name must not exceed 150 characters")
    @Schema(description = "Module name", example = "Process Monitoring", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    @Size(max = 10, message = "Prefix must not exceed 10 characters")
    @Schema(description = "Unique report-number prefix", example = "PMR", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String prefix;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Optional description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

}