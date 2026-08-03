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
@Schema(description = "A single data point for chart rendering")
public class ChartDataPoint {

    @Schema(description = "Data point label", example = "FINAL_INSPECTION", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String label;

    @Schema(description = "Data point value", example = "150", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long value;

}
