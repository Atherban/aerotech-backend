package com.aerotech.ced_ops_backend.report.chemical.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ChemicalConsumptionEntryRequest {

    @NotNull(message = "Parameter ID is required")
    private Long parameterId;

    @NotBlank(message = "Observed value is required")
    @jakarta.validation.constraints.Size(max = 500, message = "Observed value must not exceed 500 characters")
    private String observedValue;

    @jakarta.validation.constraints.Size(max = 1000, message = "Remark must not exceed 1000 characters")
    private String remark;

}
