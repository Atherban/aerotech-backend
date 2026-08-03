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
@Schema(description = "Response containing quality KPI data")
public class QualityKPIResponse {

    @Schema(description = "KPI summary cards", example = "[{\"label\":\"Approval Rate\",\"value\":\"94\",\"unit\":\"%\",\"change\":\"+2%\",\"trend\":\"up\"}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<KPICard> kpiCards;

    @Schema(description = "Daily inspection trend data", example = "[{\"date\":\"2025-06-15\",\"value\":150}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<TrendPoint> dailyInspectionTrend;

    @Schema(description = "Pass/fail counts by inspection type", example = "[{\"label\":\"FINAL_INSPECTION\",\"value\":150}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> passFailByType;

}
