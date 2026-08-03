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

    @Schema(description = "KPI summary cards", example = "[{\"label\":\"Stability Rate\",\"value\":\"98\",\"unit\":\"%\",\"change\":\"+1.5%\",\"trend\":\"up\"}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<KPICard> kpiCards;

    @Schema(description = "Out-of-specification parameters", example = "[{\"label\":\"pH\",\"value\":3}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> outOfSpecParameters;

    @Schema(description = "Failure frequency data", example = "[{\"label\":\"TEMPERATURE_DEVIATION\",\"value\":7}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> failureFrequency;

}
