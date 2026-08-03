/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.Operation
 *  io.swagger.v3.oas.annotations.Parameter
 *  io.swagger.v3.oas.annotations.media.Content
 *  io.swagger.v3.oas.annotations.media.ExampleObject
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.parameters.RequestBody
 *  io.swagger.v3.oas.annotations.responses.ApiResponse
 *  io.swagger.v3.oas.annotations.responses.ApiResponses
 *  io.swagger.v3.oas.annotations.security.SecurityRequirement
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  jakarta.validation.Valid
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.servlet.support.ServletUriComponentsBuilder
 */
package com.aerotech.ced_ops_backend.report.processmonitoring.controller;

import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.ApproveReportRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.CreateProcessMonitoringRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.SubmitReportRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.response.ProcessMonitoringResponse;
import com.aerotech.ced_ops_backend.report.processmonitoring.service.ProcessMonitoringService;
import com.aerotech.ced_ops_backend.report.support.ReportFilterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.Generated;
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

@RestController
@RequestMapping(value={"/api/reports/process-monitoring"})
@Tag(name="Process Monitoring", description="Process monitoring report APIs")
public class ProcessMonitoringController {
    private final ProcessMonitoringService processMonitoringService;

    @PostMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Create process monitoring report", description="Creates a new process monitoring report in DRAFT status", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to create a process monitoring report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=CreateProcessMonitoringRequest.class), examples={@ExampleObject(name="CreateProcessMonitoringRequestExample", summary="Example payload to create a process monitoring report", value="{\"reportDate\":\"2025-01-15\",\"shiftId\":1,\"lineId\":1,\"remarks\":\"All processes running normally\",\"entries\":[{\"parameterId\":1,\"observedValue\":\"12.5\",\"remark\":\"Within specification\"}]}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="Process monitoring report created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringResponse>> create(@Valid @RequestBody CreateProcessMonitoringRequest request) {
        ProcessMonitoringResponse response = this.processMonitoringService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(new Object[]{response.getId()}).toUri();
        return ResponseEntity.created((URI)location).body(ApiResponse.<ProcessMonitoringResponse>builder().success(true).message("Process monitoring report created successfully.").data(response).build());
    }

    @GetMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Fetch all process monitoring reports", description="Returns a list of all process monitoring reports. When pagination/filter params are provided returns a paginated PageResponse; otherwise the legacy full list.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Process monitoring reports fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<?>> getAll(@org.springdoc.core.annotations.ParameterObject ReportFilterRequest filter) {
        if (filter.hasSearchCriteria()) {
            return ResponseEntity.ok(ApiResponse.<PageResponse<ProcessMonitoringResponse>>builder().success(true).message("Process monitoring reports fetched successfully.").data(this.processMonitoringService.search(filter)).build());
        }
        return ResponseEntity.ok(ApiResponse.<List<ProcessMonitoringResponse>>builder().success(true).message("Process monitoring reports fetched successfully.").data(this.processMonitoringService.getAll()).build());
    }

    @GetMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Fetch process monitoring report by id", description="Returns a single process monitoring report by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Process monitoring report fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Process monitoring report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringResponse>> getById(@Parameter(description="ID of the process monitoring report", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ProcessMonitoringResponse>builder().success(true).message("Process monitoring report fetched successfully.").data(this.processMonitoringService.getById(id)).build());
    }

    @PostMapping(value={"/{id}/submit"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Submit process monitoring report for approval", description="Submits a DRAFT process monitoring report for approval", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to submit a process monitoring report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=SubmitReportRequest.class), examples={@ExampleObject(name="SubmitProcessMonitoringRequestExample", summary="Example payload to submit a process monitoring report", value="{\"remarks\":\"Report is ready for review\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Process monitoring report submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Process monitoring report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringResponse>> submit(@Parameter(description="ID of the process monitoring report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody SubmitReportRequest request) {
        return ResponseEntity.ok(ApiResponse.<ProcessMonitoringResponse>builder().success(true).message("Process monitoring report submitted successfully.").data(this.processMonitoringService.submit(id, request)).build());
    }

    @PostMapping(value={"/{id}/approve"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Approve process monitoring report", description="Approves a SUBMITTED process monitoring report", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to approve a process monitoring report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=ApproveReportRequest.class), examples={@ExampleObject(name="ApproveProcessMonitoringRequestExample", summary="Example payload to approve a process monitoring report", value="{\"remarks\":\"Approved - all checks passed\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Process monitoring report approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Process monitoring report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringResponse>> approve(@Parameter(description="ID of the process monitoring report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody ApproveReportRequest request) {
        return ResponseEntity.ok(ApiResponse.<ProcessMonitoringResponse>builder().success(true).message("Process monitoring report approved successfully.").data(this.processMonitoringService.approve(id, request)).build());
    }

    @PostMapping(value={"/{id}/reject"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Reject process monitoring report", description="Rejects a SUBMITTED process monitoring report", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to reject a process monitoring report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=ApproveReportRequest.class), examples={@ExampleObject(name="RejectProcessMonitoringRequestExample", summary="Example payload to reject a process monitoring report", value="{\"remarks\":\"Rejected - corrective action required\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Process monitoring report rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Process monitoring report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ProcessMonitoringResponse>> reject(@Parameter(description="ID of the process monitoring report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody ApproveReportRequest request) {
        return ResponseEntity.ok(ApiResponse.<ProcessMonitoringResponse>builder().success(true).message("Process monitoring report rejected successfully.").data(this.processMonitoringService.reject(id, request)).build());
    }

    @DeleteMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Delete draft process monitoring report", description="Deletes a DRAFT process monitoring report by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Process monitoring report deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Process monitoring report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Void> delete(@Parameter(description="ID of the process monitoring report", example="1", required=true) @PathVariable Long id) {
        this.processMonitoringService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public ProcessMonitoringController(ProcessMonitoringService processMonitoringService) {
        this.processMonitoringService = processMonitoringService;
    }
}
