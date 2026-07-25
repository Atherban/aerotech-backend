package com.aerotech.ced_ops_backend.master.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating an existing shift")
public class UpdateShiftRequest {

    @NotBlank(message = "Shift name is required")
    @Schema(description = "Name of the shift", example = "Morning")
    private String name;

    @Schema(description = "Whether the shift is active", example = "true")
    private Boolean active;

}