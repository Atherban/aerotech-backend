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
@Schema(description = "Response containing shift performance analytics")
public class ShiftPerformanceResponse {

    @Schema(description = "Reports grouped by shift")
    private List<ChartDataPoint> reportsByShift;

    @Schema(description = "Pass rate grouped by shift")
    private List<ChartDataPoint> passRateByShift;

    @Schema(description = "Failure rate grouped by shift")
    private List<ChartDataPoint> failureRateByShift;

}
