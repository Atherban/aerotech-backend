package com.aerotech.ced_ops_backend.master.process.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new process")
public class CreateProcessRequest {

    @NotBlank(message = "Process name is required")
    @Schema(description = "Name of the process", example = "Painting")
    private String name;

    @Schema(description = "Optional description of the process", example = "Surface painting and coating")
    private String description;

    @NotNull(message = "Display order is required")
    @Min(value = 1)
    @Schema(description = "Display order for sorting", example = "1")
    private Integer displayOrder;

}