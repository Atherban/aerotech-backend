package com.aerotech.ced_ops_backend.report.dailystartup.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Request payload for approving or rejecting a Daily Startup report")
public class ApproveDailyStartupRequest {

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    @Schema(description = "Approval remarks", example = "Approved - startup checks passed", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remarks;

}
