package com.aerotech.ced_ops_backend.report.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Report count grouped by production line")
public class ReportsByLineResponse {

    @Schema(description = "ID of the production line", example = "1")
    private Long lineId;

    @Schema(description = "Production line name", example = "Line A")
    private String lineName;

    @Schema(description = "Number of reports for this line", example = "40")
    private long count;

}
