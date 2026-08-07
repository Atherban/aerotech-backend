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
@Schema(description = "Lightweight parameter reference")
public class ParameterSummaryResponse {

    @Schema(description = "Parameter ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Global parameter name", example = "Temperature", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

}