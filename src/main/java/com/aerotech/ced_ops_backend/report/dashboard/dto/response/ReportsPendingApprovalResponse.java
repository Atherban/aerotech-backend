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
@Schema(description = "Number of reports pending approval")
public class ReportsPendingApprovalResponse {

    @Schema(description = "Number of reports pending approval", example = "12", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long count;

}
