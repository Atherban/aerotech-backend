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
@Schema(description = "Dashboard summary with report counts by status")
public class DashboardSummaryResponse {

    @Schema(description = "Total number of reports", example = "150", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long totalReports;

    @Schema(description = "Number of draft reports", example = "30", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long draftReports;

    @Schema(description = "Number of submitted reports", example = "20", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long submittedReports;

    @Schema(description = "Number of approved reports", example = "90", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long approvedReports;

    @Schema(description = "Number of rejected reports", example = "10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long rejectedReports;

}
