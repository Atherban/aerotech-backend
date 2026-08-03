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
@Schema(description = "Report count grouped by shift")
public class ReportsByShiftResponse {

    @Schema(description = "ID of the shift", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long shiftId;

    @Schema(description = "Shift name", example = "Morning", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String shiftName;

    @Schema(description = "Number of reports for this shift", example = "30", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long count;

}
