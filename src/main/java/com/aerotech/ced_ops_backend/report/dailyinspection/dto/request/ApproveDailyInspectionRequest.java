package com.aerotech.ced_ops_backend.report.dailyinspection.dto.request;

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
public class ApproveDailyInspectionRequest {

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String remarks;

}
