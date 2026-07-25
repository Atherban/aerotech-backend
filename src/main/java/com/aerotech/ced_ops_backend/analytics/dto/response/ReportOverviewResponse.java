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
@Schema(description = "Response containing report overview analytics")
public class ReportOverviewResponse {

    @Schema(description = "Total number of reports", example = "500")
    private long totalReports;

    @Schema(description = "Reports grouped by type")
    private List<ChartDataPoint> reportsByType;

    @Schema(description = "Reports grouped by status")
    private List<ChartDataPoint> reportsByStatus;

    @Schema(description = "Reports grouped by shift")
    private List<ChartDataPoint> reportsByShift;

    @Schema(description = "Reports grouped by line")
    private List<ChartDataPoint> reportsByLine;

}
