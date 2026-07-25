package com.aerotech.ced_ops_backend.report.dashboard.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.DashboardSummaryResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.MonthlyReportStatisticsResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.RecentReportResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsByLineResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsByShiftResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsByTypeResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsCreatedTodayResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsPendingApprovalResponse;
import com.aerotech.ced_ops_backend.report.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard report aggregation APIs")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get dashboard summary with report counts by status")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard summary fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(
                ApiResponse.<DashboardSummaryResponse>builder()
                        .success(true)
                        .message("Dashboard summary fetched successfully.")
                        .data(dashboardService.getSummary())
                        .build()
        );
    }

    @GetMapping("/reports-by-type")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get report counts grouped by type")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reports by type fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<List<ReportsByTypeResponse>>> getReportsByType() {
        return ResponseEntity.ok(
                ApiResponse.<List<ReportsByTypeResponse>>builder()
                        .success(true)
                        .message("Reports by type fetched successfully.")
                        .data(dashboardService.getReportsByType())
                        .build()
        );
    }

    @GetMapping("/reports-by-shift")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get report counts grouped by shift")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reports by shift fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<List<ReportsByShiftResponse>>> getReportsByShift() {
        return ResponseEntity.ok(
                ApiResponse.<List<ReportsByShiftResponse>>builder()
                        .success(true)
                        .message("Reports by shift fetched successfully.")
                        .data(dashboardService.getReportsByShift())
                        .build()
        );
    }

    @GetMapping("/reports-by-line")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get report counts grouped by line")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reports by line fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<List<ReportsByLineResponse>>> getReportsByLine() {
        return ResponseEntity.ok(
                ApiResponse.<List<ReportsByLineResponse>>builder()
                        .success(true)
                        .message("Reports by line fetched successfully.")
                        .data(dashboardService.getReportsByLine())
                        .build()
        );
    }

    @GetMapping("/reports-created-today")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get count of reports created today")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reports created today count fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<ReportsCreatedTodayResponse>> getReportsCreatedToday() {
        return ResponseEntity.ok(
                ApiResponse.<ReportsCreatedTodayResponse>builder()
                        .success(true)
                        .message("Reports created today count fetched successfully.")
                        .data(dashboardService.getReportsCreatedToday())
                        .build()
        );
    }

    @GetMapping("/reports-pending-approval")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get count of reports pending approval")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reports pending approval count fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<ReportsPendingApprovalResponse>> getReportsPendingApproval() {
        return ResponseEntity.ok(
                ApiResponse.<ReportsPendingApprovalResponse>builder()
                        .success(true)
                        .message("Reports pending approval count fetched successfully.")
                        .data(dashboardService.getReportsPendingApproval())
                        .build()
        );
    }

    @GetMapping("/recent-reports")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get the 10 most recent reports across all types")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recent reports fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<List<RecentReportResponse>>> getRecentReports() {
        return ResponseEntity.ok(
                ApiResponse.<List<RecentReportResponse>>builder()
                        .success(true)
                        .message("Recent reports fetched successfully.")
                        .data(dashboardService.getRecentReports())
                        .build()
        );
    }

    @GetMapping("/monthly-statistics")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get monthly report statistics")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Monthly statistics fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<List<MonthlyReportStatisticsResponse>>> getMonthlyStatistics() {
        return ResponseEntity.ok(
                ApiResponse.<List<MonthlyReportStatisticsResponse>>builder()
                        .success(true)
                        .message("Monthly statistics fetched successfully.")
                        .data(dashboardService.getMonthlyStatistics())
                        .build()
        );
    }

}
