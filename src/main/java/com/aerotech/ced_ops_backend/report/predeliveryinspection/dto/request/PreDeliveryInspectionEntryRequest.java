package com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "An individual entry within a Pre-Delivery Inspection report")
public class PreDeliveryInspectionEntryRequest {

    @NotNull(message = "Parameter ID is required")
    @Schema(description = "ID of the inspection parameter", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parameterId;

    @NotBlank(message = "Observed value is required")
    @jakarta.validation.constraints.Size(max = 500, message = "Observed value must not exceed 500 characters")
    @Schema(description = "Observed measurement value", example = "12.5", requiredMode = Schema.RequiredMode.REQUIRED)
    private String observedValue;

    @jakarta.validation.constraints.Size(max = 1000, message = "Remark must not exceed 1000 characters")
    @Schema(description = "Remark for this entry", example = "Within tolerance", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remark;

}
