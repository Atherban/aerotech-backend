package com.aerotech.ced_ops_backend.master.line.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response body for production line data")
public class LineResponse {

    @Schema(description = "Unique ID of the line", example = "1")
    private Long id;

    @Schema(description = "Name of the line", example = "Assembly Line 1")
    private String name;

    @Schema(description = "Description of the line", example = "Main assembly line for product A")
    private String description;

    @Schema(description = "Display order for sorting", example = "1")
    private Integer displayOrder;

    @Schema(description = "Whether the line is active", example = "true")
    private Boolean active;

}