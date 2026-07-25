package com.aerotech.ced_ops_backend.master.line.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating an existing production line")
public class UpdateLineRequest {

    @NotBlank(message = "Line name is required")
    @Schema(description = "Name of the line", example = "Assembly Line 1")
    private String name;

    @Schema(description = "Optional description of the line", example = "Main assembly line for product A")
    private String description;

    @NotNull(message = "Display order is required")
    @Min(1)
    @Schema(description = "Display order for sorting", example = "1")
    private Integer displayOrder;

    @Schema(description = "Whether the line is active", example = "true")
    private Boolean active;

}