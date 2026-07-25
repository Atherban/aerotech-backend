package com.aerotech.ced_ops_backend.master.process.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response body for process data")
public class ProcessResponse {

    @Schema(description = "Unique ID of the process", example = "1")
    private Long id;

    @Schema(description = "Name of the process", example = "Painting")
    private String name;

    @Schema(description = "Description of the process", example = "Surface painting and coating")
    private String description;

    @Schema(description = "Display order for sorting", example = "1")
    private Integer displayOrder;

    @Schema(description = "Whether the process is active", example = "true")
    private Boolean active;

}