package com.aerotech.ced_ops_backend.master.shift.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response body for shift data")
public class ShiftResponse {

    @Schema(description = "Unique ID of the shift", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Name of the shift", example = "Night", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Shift start time (24h)", example = "22:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Shift end time (24h)", example = "06:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalTime endTime;

    @Schema(description = "Whether the shift is active", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean active;

}
