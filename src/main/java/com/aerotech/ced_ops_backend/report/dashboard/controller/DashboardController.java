/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.Operation
 *  io.swagger.v3.oas.annotations.media.Content
 *  io.swagger.v3.oas.annotations.media.ExampleObject
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.responses.ApiResponse
 *  io.swagger.v3.oas.annotations.responses.ApiResponses
 *  io.swagger.v3.oas.annotations.security.SecurityRequirement
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package com.aerotech.ced_ops_backend.report.dashboard.controller;

import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ApprovalSummaryResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.DashboardSummaryResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.MonthlyReportStatisticsResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.RecentActivityResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.RecentReportResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsByLineResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsByShiftResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsByTypeResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsCreatedTodayResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsPendingApprovalResponse;
import com.aerotech.ced_ops_backend.report.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/reports/dashboard"})
@Tag(name="Dashboard", description="Dashboard report aggregation APIs")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping(value={"/summary"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get dashboard summary with report counts by status", description="Returns aggregated counts of reports grouped by status (draft, submitted, approved, rejected) along with the total number of reports.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Dashboard summary fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.<DashboardSummaryResponse>builder().success(true).message("Dashboard summary fetched successfully.").data(this.dashboardService.getSummary()).build());
    }

    @GetMapping(value={"/reports-by-type"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get report counts grouped by type", description="Returns the number of reports for each report type.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Reports by type fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<ReportsByTypeResponse>>> getReportsByType() {
        return ResponseEntity.ok(ApiResponse.<List<ReportsByTypeResponse>>builder().success(true).message("Reports by type fetched successfully.").data(this.dashboardService.getReportsByType()).build());
    }

    @GetMapping(value={"/reports-by-shift"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get report counts grouped by shift", description="Returns the number of reports for each shift.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Reports by shift fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<ReportsByShiftResponse>>> getReportsByShift() {
        return ResponseEntity.ok(ApiResponse.<List<ReportsByShiftResponse>>builder().success(true).message("Reports by shift fetched successfully.").data(this.dashboardService.getReportsByShift()).build());
    }

    @GetMapping(value={"/reports-by-line"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get report counts grouped by line", description="Returns the number of reports for each production line.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Reports by line fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<ReportsByLineResponse>>> getReportsByLine() {
        return ResponseEntity.ok(ApiResponse.<List<ReportsByLineResponse>>builder().success(true).message("Reports by line fetched successfully.").data(this.dashboardService.getReportsByLine()).build());
    }

    @GetMapping(value={"/reports-created-today"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get count of reports created today", description="Returns the total number of reports created today.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Reports created today count fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ReportsCreatedTodayResponse>> getReportsCreatedToday() {
        return ResponseEntity.ok(ApiResponse.<ReportsCreatedTodayResponse>builder().success(true).message("Reports created today count fetched successfully.").data(this.dashboardService.getReportsCreatedToday()).build());
    }

    @GetMapping(value={"/reports-pending-approval"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get count of reports pending approval", description="Returns the total number of reports currently pending approval.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Reports pending approval count fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ReportsPendingApprovalResponse>> getReportsPendingApproval() {
        return ResponseEntity.ok(ApiResponse.<ReportsPendingApprovalResponse>builder().success(true).message("Reports pending approval count fetched successfully.").data(this.dashboardService.getReportsPendingApproval()).build());
    }

    @GetMapping(value={"/recent-reports"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get the 10 most recent reports across all types", description="Returns the 10 most recently created reports across all report types.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Recent reports fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<RecentReportResponse>>> getRecentReports() {
        return ResponseEntity.ok(ApiResponse.<List<RecentReportResponse>>builder().success(true).message("Recent reports fetched successfully.").data(this.dashboardService.getRecentReports()).build());
    }

    @GetMapping(value={"/monthly-statistics"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get monthly report statistics", description="Returns monthly statistics for reports, including total, approved and rejected counts per month and year.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Monthly statistics fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<MonthlyReportStatisticsResponse>>> getMonthlyStatistics() {
        return ResponseEntity.ok(ApiResponse.<List<MonthlyReportStatisticsResponse>>builder().success(true).message("Monthly statistics fetched successfully.").data(this.dashboardService.getMonthlyStatistics()).build());
    }

    @GetMapping(value={"/approval-summary"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get approval summary", description="Returns aggregated approval activity: pending, approved and rejected totals, today's approvals/rejections and the overall approval rate.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Approval summary fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ApprovalSummaryResponse>> getApprovalSummary() {
        return ResponseEntity.ok(ApiResponse.<ApprovalSummaryResponse>builder().success(true).message("Approval summary fetched successfully.").data(this.dashboardService.getApprovalSummary()).build());
    }

    @GetMapping(value={"/recent-activity"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get recent report activity", description="Returns the most recent report lifecycle events (created, approved, rejected) across all report types, with the acting user and timestamp.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Recent activity fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<RecentActivityResponse>>> getRecentActivity(@org.springframework.web.bind.annotation.RequestParam(defaultValue="10") @io.swagger.v3.oas.annotations.Parameter(description="Maximum number of activity events to return", example="10") @jakarta.validation.constraints.Max(value=50) int limit) {
        return ResponseEntity.ok(ApiResponse.<List<RecentActivityResponse>>builder().success(true).message("Recent activity fetched successfully.").data(this.dashboardService.getRecentActivity(limit)).build());
    }

    @Generated
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
}
