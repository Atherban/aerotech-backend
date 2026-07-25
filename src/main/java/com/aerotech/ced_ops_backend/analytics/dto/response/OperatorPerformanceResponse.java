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
@Schema(description = "Response containing operator performance analytics")
public class OperatorPerformanceResponse {

    @Schema(description = "Reports submitted per operator")
    private List<ChartDataPoint> reportsSubmitted;

    @Schema(description = "Approval percentage per operator")
    private List<ChartDataPoint> approvalPercentage;

    @Schema(description = "Rejection percentage per operator")
    private List<ChartDataPoint> rejectionPercentage;

}
