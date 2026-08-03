/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.Operation
 *  io.swagger.v3.oas.annotations.Parameter
 *  io.swagger.v3.oas.annotations.media.Content
 *  io.swagger.v3.oas.annotations.media.ExampleObject
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.responses.ApiResponse
 *  io.swagger.v3.oas.annotations.responses.ApiResponses
 *  io.swagger.v3.oas.annotations.security.SecurityRequirement
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  lombok.Generated
 *  org.springframework.format.annotation.DateTimeFormat
 *  org.springframework.format.annotation.DateTimeFormat$ISO
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.aerotech.ced_ops_backend.analytics.controller;

import com.aerotech.ced_ops_backend.analytics.dto.response.ChemicalConsumptionKPIResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.LinePerformanceResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.OperatorPerformanceResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.ProcessMonitoringKPIResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.ProductivityKPIResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.QualityKPIResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.ReportOverviewResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.ShiftPerformanceResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.TimeAnalyticsResponse;
import com.aerotech.ced_ops_backend.analytics.service.AnalyticsService;
import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.Generated;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/analytics"})
@Tag(name="Analytics & KPIs", description="Business intelligence and KPI analytics APIs")
@SecurityRequirement(name="bearerAuth")
@PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping(value={"/report-overview"}, produces={"application/json"})
    @Operation(summary="Get report overview with counts by type, status, shift, and line", description="Returns an overview of report counts grouped by report type, status, shift, and line, optionally filtered by date range, shift, and line.")
    @SecurityRequirement(name="bearerAuth")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Report overview fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Invalid date range or parameter format", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ReportOverviewResponse>> getReportOverview(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="Start date (ISO format, e.g. 2025-01-01)", example="2025-01-01") LocalDate dateFrom, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="End date (ISO format, e.g. 2025-12-31)", example="2025-12-31") LocalDate dateTo, @RequestParam(required=false) @Parameter(description="Shift ID to filter by", example="1") Long shiftId, @RequestParam(required=false) @Parameter(description="Line ID to filter by", example="1") Long lineId) {
        return ResponseEntity.ok(ApiResponse.<ReportOverviewResponse>builder().success(true).message("Report overview fetched successfully.").data(this.analyticsService.getReportOverview(dateFrom, dateTo, shiftId, lineId)).build());
    }

    @GetMapping(value={"/quality-kpis"}, produces={"application/json"})
    @Operation(summary="Get quality KPIs including approval, rejection, pass, and fail rates", description="Returns quality KPI summary cards, daily inspection trends, and pass/fail counts by inspection type, optionally filtered by date range, shift, and line.")
    @SecurityRequirement(name="bearerAuth")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Quality KPIs fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Invalid date range or parameter format", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<QualityKPIResponse>> getQualityKPIs(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="Start date (ISO format, e.g. 2025-01-01)", example="2025-01-01") LocalDate dateFrom, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="End date (ISO format, e.g. 2025-12-31)", example="2025-12-31") LocalDate dateTo, @RequestParam(required=false) @Parameter(description="Shift ID to filter by", example="1") Long shiftId, @RequestParam(required=false) @Parameter(description="Line ID to filter by", example="1") Long lineId) {
        return ResponseEntity.ok(ApiResponse.<QualityKPIResponse>builder().success(true).message("Quality KPIs fetched successfully.").data(this.analyticsService.getQualityKPIs(dateFrom, dateTo, shiftId, lineId)).build());
    }

    @GetMapping(value={"/chemical-consumption"}, produces={"application/json"})
    @Operation(summary="Get chemical consumption KPIs with trends and line breakdown", description="Returns chemical consumption KPI summary cards, daily/weekly/monthly trends, and consumption grouped by line, optionally filtered by date range and line.")
    @SecurityRequirement(name="bearerAuth")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Chemical consumption KPIs fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Invalid date range or parameter format", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ChemicalConsumptionKPIResponse>> getChemicalConsumptionKPIs(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="Start date (ISO format, e.g. 2025-01-01)", example="2025-01-01") LocalDate dateFrom, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="End date (ISO format, e.g. 2025-12-31)", example="2025-12-31") LocalDate dateTo, @RequestParam(required=false) @Parameter(description="Line ID to filter by", example="1") Long lineId) {
        return ResponseEntity.ok(ApiResponse.<ChemicalConsumptionKPIResponse>builder().success(true).message("Chemical consumption KPIs fetched successfully.").data(this.analyticsService.getChemicalConsumptionKPIs(dateFrom, dateTo, lineId)).build());
    }

    @GetMapping(value={"/process-monitoring"}, produces={"application/json"})
    @Operation(summary="Get process monitoring KPIs including stability and failure analysis", description="Returns process monitoring KPI summary cards, out-of-specification parameters, and failure frequency data, optionally filtered by date range and line.")
    @SecurityRequirement(name="bearerAuth")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Process monitoring KPIs fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Invalid date range or parameter format", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringKPIResponse>> getProcessMonitoringKPIs(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="Start date (ISO format, e.g. 2025-01-01)", example="2025-01-01") LocalDate dateFrom, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="End date (ISO format, e.g. 2025-12-31)", example="2025-12-31") LocalDate dateTo, @RequestParam(required=false) @Parameter(description="Line ID to filter by", example="1") Long lineId) {
        return ResponseEntity.ok(ApiResponse.<ProcessMonitoringKPIResponse>builder().success(true).message("Process monitoring KPIs fetched successfully.").data(this.analyticsService.getProcessMonitoringKPIs(dateFrom, dateTo, lineId)).build());
    }

    @GetMapping(value={"/productivity"}, produces={"application/json"})
    @Operation(summary="Get productivity KPIs including reports per day, shift, operator, and approval time", description="Returns productivity KPI summary cards, reports per day trend, and reports grouped by shift and operator, optionally filtered by date range, shift, and line.")
    @SecurityRequirement(name="bearerAuth")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Productivity KPIs fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Invalid date range or parameter format", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ProductivityKPIResponse>> getProductivityKPIs(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="Start date (ISO format, e.g. 2025-01-01)", example="2025-01-01") LocalDate dateFrom, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="End date (ISO format, e.g. 2025-12-31)", example="2025-12-31") LocalDate dateTo, @RequestParam(required=false) @Parameter(description="Shift ID to filter by", example="1") Long shiftId, @RequestParam(required=false) @Parameter(description="Line ID to filter by", example="1") Long lineId) {
        return ResponseEntity.ok(ApiResponse.<ProductivityKPIResponse>builder().success(true).message("Productivity KPIs fetched successfully.").data(this.analyticsService.getProductivityKPIs(dateFrom, dateTo, shiftId, lineId)).build());
    }

    @GetMapping(value={"/time-trends"}, produces={"application/json"})
    @Operation(summary="Get time-based analytics with daily, weekly, monthly, and yearly trends", description="Returns time-based analytics trends at daily, weekly, monthly, and yearly granularity, optionally filtered by date range, shift, and line.")
    @SecurityRequirement(name="bearerAuth")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Time trends fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Invalid date range or parameter format", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<TimeAnalyticsResponse>> getTimeTrends(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="Start date (ISO format, e.g. 2025-01-01)", example="2025-01-01") LocalDate dateFrom, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="End date (ISO format, e.g. 2025-12-31)", example="2025-12-31") LocalDate dateTo, @RequestParam(required=false) @Parameter(description="Shift ID to filter by", example="1") Long shiftId, @RequestParam(required=false) @Parameter(description="Line ID to filter by", example="1") Long lineId) {
        return ResponseEntity.ok(ApiResponse.<TimeAnalyticsResponse>builder().success(true).message("Time trends fetched successfully.").data(this.analyticsService.getTimeTrends(dateFrom, dateTo, shiftId, lineId)).build());
    }

    @GetMapping(value={"/line-performance"}, produces={"application/json"})
    @Operation(summary="Get line performance analytics with rejection and approval rates", description="Returns line performance analytics with reports, rejections, and approval rate grouped by line, optionally filtered by date range.")
    @SecurityRequirement(name="bearerAuth")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Line performance fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Invalid date range or parameter format", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<LinePerformanceResponse>> getLinePerformance(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="Start date (ISO format, e.g. 2025-01-01)", example="2025-01-01") LocalDate dateFrom, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="End date (ISO format, e.g. 2025-12-31)", example="2025-12-31") LocalDate dateTo) {
        return ResponseEntity.ok(ApiResponse.<LinePerformanceResponse>builder().success(true).message("Line performance fetched successfully.").data(this.analyticsService.getLinePerformance(dateFrom, dateTo)).build());
    }

    @GetMapping(value={"/shift-performance"}, produces={"application/json"})
    @Operation(summary="Get shift performance analytics with pass and failure rates", description="Returns shift performance analytics with reports, pass rate, and failure rate grouped by shift, optionally filtered by date range.")
    @SecurityRequirement(name="bearerAuth")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Shift performance fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Invalid date range or parameter format", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ShiftPerformanceResponse>> getShiftPerformance(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="Start date (ISO format, e.g. 2025-01-01)", example="2025-01-01") LocalDate dateFrom, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="End date (ISO format, e.g. 2025-12-31)", example="2025-12-31") LocalDate dateTo) {
        return ResponseEntity.ok(ApiResponse.<ShiftPerformanceResponse>builder().success(true).message("Shift performance fetched successfully.").data(this.analyticsService.getShiftPerformance(dateFrom, dateTo)).build());
    }

    @GetMapping(value={"/operator-performance"}, produces={"application/json"})
    @Operation(summary="Get operator performance analytics with approval and rejection percentages", description="Returns operator performance analytics with reports submitted, approval percentage, and rejection percentage per operator, optionally filtered by date range.")
    @SecurityRequirement(name="bearerAuth")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Operator performance fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Invalid date range or parameter format", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<OperatorPerformanceResponse>> getOperatorPerformance(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="Start date (ISO format, e.g. 2025-01-01)", example="2025-01-01") LocalDate dateFrom, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) @Parameter(description="End date (ISO format, e.g. 2025-12-31)", example="2025-12-31") LocalDate dateTo) {
        return ResponseEntity.ok(ApiResponse.<OperatorPerformanceResponse>builder().success(true).message("Operator performance fetched successfully.").data(this.analyticsService.getOperatorPerformance(dateFrom, dateTo)).build());
    }

    @Generated
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
}
