package com.aerotech.ced_ops_backend.master.reporttype.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A fixed, predefined report type in the catalog")
public class ReportTypeResponse {

    @Schema(description = "Report type code", example = "CHEMICAL_CONSUMPTION", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String code;

    @Schema(description = "Human readable report type name", example = "Chemical Consumption", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

}
