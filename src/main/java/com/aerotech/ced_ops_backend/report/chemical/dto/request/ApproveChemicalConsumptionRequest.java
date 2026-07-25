package com.aerotech.ced_ops_backend.report.chemical.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class ApproveChemicalConsumptionRequest {

    @NotNull(message = "Approval decision must be specified")
    private Boolean approved;

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String remarks;

}
