package com.aerotech.ced_ops_backend.master.module.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Request to create a Process within a template version")
public class CreateProcessRequest {

    @NotNull(message = "Template version ID is required")
    @Schema(description = "ID of the template version that owns this process", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long templateVersionId;

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    @Schema(description = "Process name", example = "CED Coating", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Optional description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @NotNull(message = "displayOrder is required")
    @Min(1)
    @Schema(description = "The ONLY ordering mechanism. Must be respected across the application.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayOrder;

}