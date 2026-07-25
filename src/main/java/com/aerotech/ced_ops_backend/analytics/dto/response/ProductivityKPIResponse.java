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

    @Schema(description = "KPI summary cards")
    private List<KPICard> kpiCards;

    @Schema(description = "Reports submitted per day")
    private List<TrendPoint> reportsPerDay;

    @Schema(description = "Reports grouped by shift")
    private List<ChartDataPoint> reportsPerShift;

    @Schema(description = "Reports grouped by operator")
    private List<ChartDataPoint> reportsPerOperator;

}
