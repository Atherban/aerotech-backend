package com.aerotech.ced_ops_backend.report.processmonitoring.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for submitting a Process Monitoring report")
public class SubmitReportRequest {

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    @Schema(description = "Submission remarks", example = "Report is ready for review")
    private String remarks;

}