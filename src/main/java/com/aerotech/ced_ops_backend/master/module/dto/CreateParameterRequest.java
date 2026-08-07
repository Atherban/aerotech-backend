package com.aerotech.ced_ops_backend.master.module.dto;

import com.aerotech.ced_ops_backend.common.enums.InputType;
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
@Schema(description = "Request to create a global reusable Parameter")
public class CreateParameterRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    @Schema(description = "Global parameter name (exists exactly once)", example = "Temperature", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "inputType is required")
    @Schema(description = "Input type of the parameter", example = "NUMBER", requiredMode = Schema.RequiredMode.REQUIRED)
    private InputType inputType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Optional description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

}