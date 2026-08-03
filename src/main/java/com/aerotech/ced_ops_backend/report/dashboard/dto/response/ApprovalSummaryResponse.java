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
@Schema(description = "Summary of report approval activity")
public class ApprovalSummaryResponse {

    @Schema(description = "Number of reports pending approval", example = "12", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long pendingApprovals;

    @Schema(description = "Total number of approved reports", example = "90", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long approvedReports;

    @Schema(description = "Total number of rejected reports", example = "10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long rejectedReports;

    @Schema(description = "Number of reports approved today", example = "3", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long approvedToday;

    @Schema(description = "Number of reports rejected today", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long rejectedToday;

    @Schema(description = "Approval rate (0-100), approved of all decided reports", example = "90.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private double approvalRate;

}
