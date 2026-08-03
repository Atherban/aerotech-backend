package com.aerotech.ced_ops_backend.analytics.dto.response;

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
@Schema(description = "A single KPI card displaying a metric")
public class KPICard {

    @Schema(description = "KPI label", example = "Total Reports", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String label;

    @Schema(description = "KPI value", example = "500", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String value;

    @Schema(description = "Unit of measurement", example = "reports", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String unit;

    @Schema(description = "Change percentage", example = "+12.5%", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String change;

    @Schema(description = "Trend direction", example = "up", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String trend;

}
