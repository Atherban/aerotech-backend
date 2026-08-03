package com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request;

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
@Schema(description = "Request payload for approving or rejecting a First Piece Inspection report")
public class ApproveFirstPieceInspectionRequest {

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    @Schema(description = "Approval or rejection remarks", example = "Approved - all checks passed", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remarks;

}
