package com.aerotech.ced_ops_backend.master.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response body for shift data")
public class ShiftResponse {

    @Schema(description = "Unique ID of the shift", example = "1")
    private Long id;

    @Schema(description = "Name of the shift", example = "Morning")
    private String name;

    @Schema(description = "Whether the shift is active", example = "true")
    private Boolean active;

}