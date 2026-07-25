package com.aerotech.ced_ops_backend.report.processmonitoring.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.ApproveReportRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.CreateProcessMonitoringRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.SubmitReportRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.response.ProcessMonitoringResponse;
import com.aerotech.ced_ops_backend.report.processmonitoring.service.ProcessMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reports/process-monitoring")
@RequiredArgsConstructor
@Tag(name = "Process Monitoring", description = "Process monitoring report APIs")
public class ProcessMonitoringController {

    private final ProcessMonitoringService processMonitoringService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create process monitoring report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Process monitoring report created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringResponse>> create(
            @Valid @RequestBody CreateProcessMonitoringRequest request
    ) {
        ProcessMonitoringResponse response = processMonitoringService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<ProcessMonitoringResponse>builder()
                        .success(true)
                        .message("Process monitoring report created successfully.")
                        .data(response)
                        .build()
        );

    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Fetch all process monitoring reports")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Process monitoring reports fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<List<ProcessMonitoringResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.<List<ProcessMonitoringResponse>>builder()
                        .success(true)
                        .message("Process monitoring reports fetched successfully.")
                        .data(processMonitoringService.getAll())
                        .build()
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Fetch process monitoring report by id")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Process monitoring report fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Process monitoring report not found")
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringResponse>> getById(
            @io.swagger.v3.oas.annotations.Parameter(description = "Process monitoring report ID") @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ProcessMonitoringResponse>builder()
                        .success(true)
                        .message("Process monitoring report fetched successfully.")
                        .data(processMonitoringService.getById(id))
                        .build()
        );

    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Submit process monitoring report for approval")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Process monitoring report submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Process monitoring report not found")
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringResponse>> submit(
            @io.swagger.v3.oas.annotations.Parameter(description = "Process monitoring report ID") @PathVariable Long id,
            @Valid @RequestBody SubmitReportRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ProcessMonitoringResponse>builder()
                        .success(true)
                        .message("Process monitoring report submitted successfully.")
                        .data(processMonitoringService.submit(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve process monitoring report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Process monitoring report approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN or ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Process monitoring report not found")
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringResponse>> approve(
            @io.swagger.v3.oas.annotations.Parameter(description = "Process monitoring report ID") @PathVariable Long id,
            @Valid @RequestBody ApproveReportRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ProcessMonitoringResponse>builder()
                        .success(true)
                        .message("Process monitoring report approved successfully.")
                        .data(processMonitoringService.approve(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reject process monitoring report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Process monitoring report rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN or ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Process monitoring report not found")
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringResponse>> reject(
            @io.swagger.v3.oas.annotations.Parameter(description = "Process monitoring report ID") @PathVariable Long id,
            @Valid @RequestBody ApproveReportRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ProcessMonitoringResponse>builder()
                        .success(true)
                        .message("Process monitoring report rejected successfully.")
                        .data(processMonitoringService.reject(id, request))
                        .build()
        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete draft process monitoring report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Process monitoring report deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Process monitoring report not found")
    })
    public ResponseEntity<Void> delete(
            @io.swagger.v3.oas.annotations.Parameter(description = "Process monitoring report ID") @PathVariable Long id
    ) {
        processMonitoringService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
