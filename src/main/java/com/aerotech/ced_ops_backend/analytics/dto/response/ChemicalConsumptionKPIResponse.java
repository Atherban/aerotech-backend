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

    @Schema(description = "KPI summary cards")
    private List<KPICard> kpiCards;

    @Schema(description = "Daily consumption trend")
    private List<TrendPoint> dailyTrend;

    @Schema(description = "Weekly consumption trend")
    private List<TrendPoint> weeklyTrend;

    @Schema(description = "Monthly consumption trend")
    private List<TrendPoint> monthlyTrend;

    @Schema(description = "Consumption grouped by line")
    private List<ChartDataPoint> consumptionByLine;

}
