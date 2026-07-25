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
@Schema(description = "Monthly report statistics")
public class MonthlyReportStatisticsResponse {

    @Schema(description = "Year", example = "2025")
    private int year;

    @Schema(description = "Month (1-12)", example = "1")
    private int month;

    @Schema(description = "Total number of reports in the month", example = "40")
    private long totalReports;

    @Schema(description = "Number of approved reports in the month", example = "30")
    private long approvedReports;

    @Schema(description = "Number of rejected reports in the month", example = "5")
    private long rejectedReports;

}
