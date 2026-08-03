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
@Schema(description = "Response containing line performance analytics")
public class LinePerformanceResponse {

    @Schema(description = "Reports grouped by line", example = "[{\"label\":\"LINE_1\",\"value\":200}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> reportsByLine;

    @Schema(description = "Rejections grouped by line", example = "[{\"label\":\"LINE_1\",\"value\":15}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> rejectionsByLine;

    @Schema(description = "Approval rate grouped by line", example = "[{\"label\":\"LINE_1\",\"value\":92}]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ChartDataPoint> approvalRateByLine;

}
