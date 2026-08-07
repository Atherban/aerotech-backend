package com.aerotech.ced_ops_backend.master.module.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Request to create a new template version of a module (snapshot of the current ACTIVE version plus a new change note)")
public class CreateTemplateVersionRequest {

    @NotBlank(message = "Change note is required")
    @Size(max = 500, message = "Change note must not exceed 500 characters")
    @Schema(description = "Note describing what changed in this template version", example = "Added Final Inspection process", requiredMode = Schema.RequiredMode.REQUIRED)
    private String changeNote;

}