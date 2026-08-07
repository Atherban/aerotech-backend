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
@Schema(description = "Request to update a Module Type")
public class UpdateModuleTypeRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Module type name", example = "Quality", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    @Size(max = 300, message = "Description must not exceed 300 characters")
    @Schema(description = "Optional description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

}