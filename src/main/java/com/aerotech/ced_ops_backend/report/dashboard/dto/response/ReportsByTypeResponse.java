package com.aerotech.ced_ops_backend.report.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Report count grouped by report type")
public class ReportsByTypeResponse {

    @Schema(description = "Report type", example = "FIRST_PIECE_INSPECTION", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportType;

    @Schema(description = "Number of reports of this type", example = "50", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long count;

}
