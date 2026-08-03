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
@Schema(description = "Response containing chemical consumption KPI data")
public class ChemicalConsumptionKPIResponse {

    @Schema(description = "KPI summary cards", example = "[{\"label\":\"Total Consumption\",\"value\":\"1200\",\"unit\":\"L\",\"change\":\"+5%\",\"trend\":\"up\"}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<KPICard> kpiCards;

    @Schema(description = "Daily consumption trend", example = "[{\"date\":\"2025-06-15\",\"value\":42}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<TrendPoint> dailyTrend;

    @Schema(description = "Weekly consumption trend", example = "[{\"date\":\"2025-06-15\",\"value\":280}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<TrendPoint> weeklyTrend;

    @Schema(description = "Monthly consumption trend", example = "[{\"date\":\"2025-06-01\",\"value\":1200}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<TrendPoint> monthlyTrend;

    @Schema(description = "Consumption grouped by line", example = "[{\"label\":\"LINE_1\",\"value\":600}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> consumptionByLine;

}
