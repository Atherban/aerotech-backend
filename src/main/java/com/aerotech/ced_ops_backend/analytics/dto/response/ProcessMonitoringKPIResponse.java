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
@Schema(description = "Response containing process monitoring KPI data")
public class ProcessMonitoringKPIResponse {

    @Schema(description = "KPI summary cards")
    private List<KPICard> kpiCards;

    @Schema(description = "Out-of-specification parameters")
    private List<ChartDataPoint> outOfSpecParameters;

    @Schema(description = "Failure frequency data")
    private List<ChartDataPoint> failureFrequency;

}
