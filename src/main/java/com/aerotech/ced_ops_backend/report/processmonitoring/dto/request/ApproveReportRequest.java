package com.aerotech.ced_ops_backend.report.processmonitoring.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for approving a Process Monitoring report")
public class ApproveReportRequest {

    @jakarta.validation.constraints.NotNull(message = "Approval decision must be specified")
    @Schema(description = "Approval decision", example = "true")
    private Boolean approved;

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    @Schema(description = "Approval remarks", example = "Approved - all processes within specification")
    private String remarks;

}