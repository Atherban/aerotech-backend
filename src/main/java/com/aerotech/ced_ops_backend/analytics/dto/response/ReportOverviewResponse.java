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

    @Schema(description = "Total number of reports", example = "500", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long totalReports;

    @Schema(description = "Reports grouped by type", example = "[{\"label\":\"FINAL_INSPECTION\",\"value\":300}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> reportsByType;

    @Schema(description = "Reports grouped by status", example = "[{\"label\":\"APPROVED\",\"value\":350}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> reportsByStatus;

    @Schema(description = "Reports grouped by shift", example = "[{\"label\":\"MORNING\",\"value\":250}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> reportsByShift;

    @Schema(description = "Reports grouped by line", example = "[{\"label\":\"LINE_1\",\"value\":200}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> reportsByLine;

}
