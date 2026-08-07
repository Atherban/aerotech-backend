package com.aerotech.ced_ops_backend.master.module.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request to create a Module (reusable report template)")
public class CreateModuleRequest {

    @NotNull(message = "Module type is required")
    @Schema(description = "ID of the owning module type", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long moduleTypeId;

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    @Schema(description = "Module name", example = "Process Monitoring", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Prefix is required")
    @Size(max = 10, message = "Prefix must not exceed 10 characters")
    @Schema(description = "Unique report-number prefix", example = "PMR", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prefix;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Optional description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @NotBlank(message = "Version change note is required")
    @Size(max = 500, message = "Change note must not exceed 500 characters")
    @Schema(description = "Note for the initial template version", example = "Initial template", requiredMode = Schema.RequiredMode.REQUIRED)
    private String changeNote;

}