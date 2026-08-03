package com.aerotech.ced_ops_backend.master.shift.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating an existing shift")
public class UpdateShiftRequest {

    @NotBlank(message = "Shift name is required")
    @Schema(description = "Name of the shift", example = "Night", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Shift start time (24h)", example = "22:00", type = "string", format = "HH:mm", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Shift end time (24h)", example = "06:00", type = "string", format = "HH:mm", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalTime endTime;

    @Schema(description = "Whether the shift is active", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean active;

}
