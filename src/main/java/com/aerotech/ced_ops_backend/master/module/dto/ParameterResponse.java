package com.aerotech.ced_ops_backend.master.module.dto;

import com.aerotech.ced_ops_backend.common.enums.InputType;
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
@Schema(description = "Global reusable Parameter data")
public class ParameterResponse {

    @Schema(description = "Parameter ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Global parameter name", example = "Temperature", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Input type of the parameter", example = "NUMBER", requiredMode = Schema.RequiredMode.REQUIRED)
    private InputType inputType;

    @Schema(description = "Optional description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "Whether the parameter is active", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean active;

}