package com.aerotech.ced_ops_backend.analytics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing productivity KPI data")
public class ProductivityKPIResponse {

    @Schema(description = "KPI summary cards", example = "[{\"label\":\"Reports per Day\",\"value\":\"42\",\"unit\":\"reports\",\"change\":\"+8%\",\"trend\":\"up\"}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<KPICard> kpiCards;

    @Schema(description = "Reports submitted per day", example = "[{\"date\":\"2025-06-15\",\"value\":42}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<TrendPoint> reportsPerDay;

    @Schema(description = "Reports grouped by shift", example = "[{\"label\":\"MORNING\",\"value\":180}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> reportsPerShift;

    @Schema(description = "Reports grouped by operator", example = "[{\"label\":\"john.doe\",\"value\":120}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> reportsPerOperator;

}
