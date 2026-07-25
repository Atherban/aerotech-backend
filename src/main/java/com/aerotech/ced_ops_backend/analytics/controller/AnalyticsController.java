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
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics & KPIs", description = "Business intelligence and KPI analytics APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/report-overview")
    @Operation(summary = "Get report overview with counts by type, status, shift, and line")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Report overview fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<ReportOverviewResponse>> getReportOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date") LocalDate dateTo,
            @RequestParam(required = false) @Parameter(description = "Shift ID") Long shiftId,
            @RequestParam(required = false) @Parameter(description = "Line ID") Long lineId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<ReportOverviewResponse>builder()
                        .success(true)
                        .message("Report overview fetched successfully.")
                        .data(analyticsService.getReportOverview(dateFrom, dateTo, shiftId, lineId))
                        .build()
        );
    }

    @GetMapping("/quality-kpis")
    @Operation(summary = "Get quality KPIs including approval, rejection, pass, and fail rates")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Quality KPIs fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<QualityKPIResponse>> getQualityKPIs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date") LocalDate dateTo,
            @RequestParam(required = false) @Parameter(description = "Shift ID") Long shiftId,
            @RequestParam(required = false) @Parameter(description = "Line ID") Long lineId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<QualityKPIResponse>builder()
                        .success(true)
                        .message("Quality KPIs fetched successfully.")
                        .data(analyticsService.getQualityKPIs(dateFrom, dateTo, shiftId, lineId))
                        .build()
        );
    }

    @GetMapping("/chemical-consumption")
    @Operation(summary = "Get chemical consumption KPIs with trends and line breakdown")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Chemical consumption KPIs fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<ChemicalConsumptionKPIResponse>> getChemicalConsumptionKPIs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date") LocalDate dateTo,
            @RequestParam(required = false) @Parameter(description = "Line ID") Long lineId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<ChemicalConsumptionKPIResponse>builder()
                        .success(true)
                        .message("Chemical consumption KPIs fetched successfully.")
                        .data(analyticsService.getChemicalConsumptionKPIs(dateFrom, dateTo, lineId))
                        .build()
        );
    }

    @GetMapping("/process-monitoring")
    @Operation(summary = "Get process monitoring KPIs including stability and failure analysis")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Process monitoring KPIs fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringKPIResponse>> getProcessMonitoringKPIs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date") LocalDate dateTo,
            @RequestParam(required = false) @Parameter(description = "Line ID") Long lineId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<ProcessMonitoringKPIResponse>builder()
                        .success(true)
                        .message("Process monitoring KPIs fetched successfully.")
                        .data(analyticsService.getProcessMonitoringKPIs(dateFrom, dateTo, lineId))
                        .build()
        );
    }

    @GetMapping("/productivity")
    @Operation(summary = "Get productivity KPIs including reports per day, shift, operator, and approval time")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Productivity KPIs fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<ProductivityKPIResponse>> getProductivityKPIs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date") LocalDate dateTo,
            @RequestParam(required = false) @Parameter(description = "Shift ID") Long shiftId,
            @RequestParam(required = false) @Parameter(description = "Line ID") Long lineId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<ProductivityKPIResponse>builder()
                        .success(true)
                        .message("Productivity KPIs fetched successfully.")
                        .data(analyticsService.getProductivityKPIs(dateFrom, dateTo, shiftId, lineId))
                        .build()
        );
    }

    @GetMapping("/time-trends")
    @Operation(summary = "Get time-based analytics with daily, weekly, monthly, and yearly trends")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Time trends fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<TimeAnalyticsResponse>> getTimeTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date") LocalDate dateTo,
            @RequestParam(required = false) @Parameter(description = "Shift ID") Long shiftId,
            @RequestParam(required = false) @Parameter(description = "Line ID") Long lineId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<TimeAnalyticsResponse>builder()
                        .success(true)
                        .message("Time trends fetched successfully.")
                        .data(analyticsService.getTimeTrends(dateFrom, dateTo, shiftId, lineId))
                        .build()
        );
    }

    @GetMapping("/line-performance")
    @Operation(summary = "Get line performance analytics with rejection and approval rates")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Line performance fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<LinePerformanceResponse>> getLinePerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date") LocalDate dateTo
    ) {
        return ResponseEntity.ok(
                ApiResponse.<LinePerformanceResponse>builder()
                        .success(true)
                        .message("Line performance fetched successfully.")
                        .data(analyticsService.getLinePerformance(dateFrom, dateTo))
                        .build()
        );
    }

    @GetMapping("/shift-performance")
    @Operation(summary = "Get shift performance analytics with pass and failure rates")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shift performance fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<ShiftPerformanceResponse>> getShiftPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date") LocalDate dateTo
    ) {
        return ResponseEntity.ok(
                ApiResponse.<ShiftPerformanceResponse>builder()
                        .success(true)
                        .message("Shift performance fetched successfully.")
                        .data(analyticsService.getShiftPerformance(dateFrom, dateTo))
                        .build()
        );
    }

    @GetMapping("/operator-performance")
    @Operation(summary = "Get operator performance analytics with approval and rejection percentages")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operator performance fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<OperatorPerformanceResponse>> getOperatorPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date") LocalDate dateTo
    ) {
        return ResponseEntity.ok(
                ApiResponse.<OperatorPerformanceResponse>builder()
                        .success(true)
                        .message("Operator performance fetched successfully.")
                        .data(analyticsService.getOperatorPerformance(dateFrom, dateTo))
                        .build()
        );
    }

}
