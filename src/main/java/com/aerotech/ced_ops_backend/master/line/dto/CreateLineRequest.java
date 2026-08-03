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
@Schema(description = "Request body for creating a new production line")
public class CreateLineRequest {

    @NotBlank(message = "Line name is required")
    @Schema(description = "Name of the line", example = "Assembly Line 1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Optional description of the line", example = "Main assembly line for product A", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @NotNull(message = "Display order is required")
    @Min(1)
    @Schema(description = "Display order for sorting", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayOrder;

}
