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
@Schema(description = "Request payload for submitting a Daily Startup report")
public class SubmitDailyStartupRequest {

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    @Schema(description = "Submission remarks", example = "Ready for review", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remarks;

}
