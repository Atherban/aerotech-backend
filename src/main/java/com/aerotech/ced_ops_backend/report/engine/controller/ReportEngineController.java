package com.aerotech.ced_ops_backend.report.engine.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.report.engine.dto.CompletedReportResponse;
import com.aerotech.ced_ops_backend.report.engine.dto.RecordProcessRequest;
import com.aerotech.ced_ops_backend.report.engine.dto.RecordProcessResponse;
import com.aerotech.ced_ops_backend.report.engine.dto.RecordedProcessItem;
import com.aerotech.ced_ops_backend.report.engine.dto.ReportProcessStep;
import com.aerotech.ced_ops_backend.report.engine.dto.ReportSessionResponse;
import com.aerotech.ced_ops_backend.report.engine.dto.StartReportRequest;
import com.aerotech.ced_ops_backend.report.engine.service.GenericReportEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Configuration-driven report engine endpoints. No report-specific logic: every
 * process and field comes from the module configuration. The backend controls
 * navigation; the frontend only renders what is returned.
 */
@RestController
@RequestMapping("/api/report-engine")
@Tag(name = "Report Engine (Module-driven)", description = "Configuration-driven report workflow: sessions, save & next, save & submit")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ReportEngineController {

    private final GenericReportEngineService engineService;

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATOR')")
    @Operation(summary = "Start a report session",
            description = "Creates a session that freezes the module's latest ACTIVE template version and returns the session plus its first process.")
    public ResponseEntity<ApiResponse<ReportSessionResponse>> start(@Valid @RequestBody StartReportRequest request) {
        return ResponseEntity.ok(ApiResponse.<ReportSessionResponse>builder()
                .success(true)
                .message("Report session started.")
                .data(engineService.start(
                        engineService.getModuleOrThrow(request.getModuleId()),
                        engineService.currentUser(),
                        request.getShiftId(),
                        request.getLineId()))
                .build());
    }

    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a report session snapshot", description = "Fetches the current state of a work-in-progress session.")
    public ResponseEntity<ApiResponse<ReportSessionResponse>> getSession(
            @Parameter(description = "Session ID", example = "1", required = true) @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.<ReportSessionResponse>builder()
                .success(true)
                .message("Session fetched successfully.")
                .data(engineService.getSessionResponse(sessionId))
                .build());
    }

    @GetMapping("/sessions/{sessionId}/current")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Load the current process step",
            description = "Returns the process the frontend must render next, with all its configured fields.")
    public ResponseEntity<ApiResponse<ReportProcessStep>> current(
            @Parameter(description = "Session ID", example = "1", required = true) @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.<ReportProcessStep>builder()
                .success(true)
                .message("Current process loaded.")
                .data(engineService.getCurrentProcess(sessionId))
                .build());
    }

    @PostMapping("/sessions/{sessionId}/save-next")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATOR')")
    @Operation(summary = "Save current process and return the next step",
            description = "Records the current process' values, advances to the next process by displayOrder, and returns it. When it was the last process, the report is completed.")
    public ResponseEntity<ApiResponse<RecordProcessResponse>> saveNext(
            @Parameter(description = "Session ID", example = "1", required = true) @PathVariable Long sessionId,
            @Valid @RequestBody RecordProcessRequest request) {
        return ResponseEntity.ok(ApiResponse.<RecordProcessResponse>builder()
                .success(true)
                .message("Process saved.")
                .data(engineService.saveAndNext(sessionId, request))
                .build());
    }

    @PostMapping("/sessions/{sessionId}/save-submit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATOR')")
    @Operation(summary = "Save the final process and submit the report",
            description = "Records the current process' values and immediately completes and submits the report.")
    public ResponseEntity<ApiResponse<RecordProcessResponse>> saveSubmit(
            @Parameter(description = "Session ID", example = "1", required = true) @PathVariable Long sessionId,
            @Valid @RequestBody RecordProcessRequest request) {
        return ResponseEntity.ok(ApiResponse.<RecordProcessResponse>builder()
                .success(true)
                .message("Report submitted.")
                .data(engineService.saveAndSubmit(sessionId, request))
                .build());
    }

    @GetMapping("/sessions/{sessionId}/recorded")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recorded processes with grouped values", description = "Fetches all recorded processes and their grouped recorded values for a session.")
    public ResponseEntity<ApiResponse<List<RecordedProcessItem>>> recorded(
            @Parameter(description = "Session ID", example = "1", required = true) @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.<List<RecordedProcessItem>>builder()
                .success(true)
                .message("Recorded processes fetched successfully.")
                .data(engineService.getRecordedProcesses(sessionId))
                .build());
    }

    @GetMapping("/reports/{reportId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a submitted report by ID", description = "Fetches a completed (submitted) report.")
    public ResponseEntity<ApiResponse<CompletedReportResponse>> getReport(
            @Parameter(description = "Report ID", example = "1", required = true) @PathVariable Long reportId) {
        return ResponseEntity.ok(ApiResponse.<CompletedReportResponse>builder()
                .success(true)
                .message("Report fetched successfully.")
                .data(engineService.getCompletedReport(reportId))
                .build());
    }

    @GetMapping("/reports/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List my submitted reports", description = "Fetches the current user's completed (submitted) reports.")
    public ResponseEntity<ApiResponse<List<CompletedReportResponse>>> myReports() {
        return ResponseEntity.ok(ApiResponse.<List<CompletedReportResponse>>builder()
                .success(true)
                .message("Reports fetched successfully.")
                .data(engineService.getMyReports(engineService.currentUser().getId()))
                .build());
    }

    @GetMapping("/sessions/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List my in-progress sessions", description = "Fetches the current user's in-progress report sessions.")
    public ResponseEntity<ApiResponse<List<ReportSessionResponse>>> mySessions() {
        return ResponseEntity.ok(ApiResponse.<List<ReportSessionResponse>>builder()
                .success(true)
                .message("Sessions fetched successfully.")
                .data(engineService.getMySessions(engineService.currentUser().getId()))
                .build());
    }

}