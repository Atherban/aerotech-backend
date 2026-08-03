package com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request;

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
@Schema(description = "Request payload for submitting a Pre-Delivery Inspection report")
public class SubmitPreDeliveryInspectionRequest {

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    @Schema(description = "Submission remarks", example = "Report is ready for review", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remarks;

}
