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
@Schema(description = "Response containing time-based analytics data")
public class TimeAnalyticsResponse {

    @Schema(description = "Daily trend data points")
    private List<TrendPoint> dailyTrend;

    @Schema(description = "Weekly trend data points")
    private List<TrendPoint> weeklyTrend;

    @Schema(description = "Monthly trend data points")
    private List<TrendPoint> monthlyTrend;

    @Schema(description = "Yearly trend data points")
    private List<TrendPoint> yearlyTrend;

}
