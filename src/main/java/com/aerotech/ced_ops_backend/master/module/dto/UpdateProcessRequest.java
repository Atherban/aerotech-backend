package com.aerotech.ced_ops_backend.master.module.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Request to update a Process")
public class UpdateProcessRequest {

    @Size(max = 150, message = "Name must not exceed 150 characters")
    @Schema(description = "Process name", example = "CED Coating", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Optional description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Min(1)
    @Schema(description = "The ONLY ordering mechanism", example = "2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer displayOrder;

}