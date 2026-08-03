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
package com.aerotech.ced_ops_backend.report.dailyinspection.controller;

import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.ApproveDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.CreateDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.SubmitDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.response.DailyInspectionResponse;
import com.aerotech.ced_ops_backend.report.dailyinspection.service.DailyInspectionService;
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
@RequestMapping(value={"/api/reports/daily-inspection"})
@Tag(name="Daily Inspection", description="Daily inspection report APIs")
public class DailyInspectionController {
    private final DailyInspectionService dailyInspectionService;

    @PostMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Create daily inspection report", description="Creates a new daily inspection report in DRAFT status", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to create a daily inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=CreateDailyInspectionRequest.class), examples={@ExampleObject(name="CreateDailyInspectionRequestExample", summary="Example payload to create a daily inspection report", value="{\"reportDate\":\"2025-01-15\",\"shiftId\":1,\"lineId\":1,\"inspectorName\":\"Jane Smith\",\"correctiveAction\":\"Re-adjusted the machine\",\"remarks\":\"All checks completed\",\"entries\":[{\"parameterId\":1,\"observedValue\":\"12.5\",\"remark\":\"Within specification\"}]}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="Daily inspection report created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<DailyInspectionResponse>> create(@Valid @RequestBody CreateDailyInspectionRequest request) {
        DailyInspectionResponse response = this.dailyInspectionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(new Object[]{response.getId()}).toUri();
        return ResponseEntity.created((URI)location).body(ApiResponse.<DailyInspectionResponse>builder().success(true).message("Daily inspection report created successfully.").data(response).build());
    }

    @GetMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Fetch all daily inspection reports", description="Returns a list of all daily inspection reports. When pagination/filter params are provided returns a paginated PageResponse; otherwise the legacy full list.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Daily inspection reports fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<?>> getAll(@org.springdoc.core.annotations.ParameterObject ReportFilterRequest filter) {
        if (filter.hasSearchCriteria()) {
            return ResponseEntity.ok(ApiResponse.<PageResponse<DailyInspectionResponse>>builder().success(true).message("Daily inspection reports fetched successfully.").data(this.dailyInspectionService.search(filter)).build());
        }
        return ResponseEntity.ok(ApiResponse.<List<DailyInspectionResponse>>builder().success(true).message("Daily inspection reports fetched successfully.").data(this.dailyInspectionService.getAll()).build());
    }

    @GetMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Fetch daily inspection report by id", description="Returns a single daily inspection report by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Daily inspection report fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Daily inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<DailyInspectionResponse>> getById(@Parameter(description="ID of the daily inspection report", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<DailyInspectionResponse>builder().success(true).message("Daily inspection report fetched successfully.").data(this.dailyInspectionService.getById(id)).build());
    }

    @PostMapping(value={"/{id}/submit"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Submit daily inspection report for approval", description="Submits a DRAFT daily inspection report for approval", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to submit a daily inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=SubmitDailyInspectionRequest.class), examples={@ExampleObject(name="SubmitDailyInspectionRequestExample", summary="Example payload to submit a daily inspection report", value="{\"remarks\":\"Ready for review\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Daily inspection report submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Daily inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<DailyInspectionResponse>> submit(@Parameter(description="ID of the daily inspection report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody SubmitDailyInspectionRequest request) {
        return ResponseEntity.ok(ApiResponse.<DailyInspectionResponse>builder().success(true).message("Daily inspection report submitted successfully.").data(this.dailyInspectionService.submit(id, request)).build());
    }

    @PostMapping(value={"/{id}/approve"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Approve daily inspection report", description="Approves a SUBMITTED daily inspection report", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to approve a daily inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=ApproveDailyInspectionRequest.class), examples={@ExampleObject(name="ApproveDailyInspectionRequestExample", summary="Example payload to approve a daily inspection report", value="{\"remarks\":\"Approved - all checks passed\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Daily inspection report approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Daily inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<DailyInspectionResponse>> approve(@Parameter(description="ID of the daily inspection report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody ApproveDailyInspectionRequest request) {
        return ResponseEntity.ok(ApiResponse.<DailyInspectionResponse>builder().success(true).message("Daily inspection report approved successfully.").data(this.dailyInspectionService.approve(id, request)).build());
    }

    @PostMapping(value={"/{id}/reject"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Reject daily inspection report", description="Rejects a SUBMITTED daily inspection report", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to reject a daily inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=ApproveDailyInspectionRequest.class), examples={@ExampleObject(name="RejectDailyInspectionRequestExample", summary="Example payload to reject a daily inspection report", value="{\"remarks\":\"Rejected - measurements out of specification\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Daily inspection report rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Daily inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<DailyInspectionResponse>> reject(@Parameter(description="ID of the daily inspection report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody ApproveDailyInspectionRequest request) {
        return ResponseEntity.ok(ApiResponse.<DailyInspectionResponse>builder().success(true).message("Daily inspection report rejected successfully.").data(this.dailyInspectionService.reject(id, request)).build());
    }

    @DeleteMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Delete draft daily inspection report", description="Deletes a DRAFT daily inspection report by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Daily inspection report deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Daily inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Void> delete(@Parameter(description="ID of the daily inspection report", example="1", required=true) @PathVariable Long id) {
        this.dailyInspectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public DailyInspectionController(DailyInspectionService dailyInspectionService) {
        this.dailyInspectionService = dailyInspectionService;
    }
}
