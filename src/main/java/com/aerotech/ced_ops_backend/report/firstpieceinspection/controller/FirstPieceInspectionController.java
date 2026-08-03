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
package com.aerotech.ced_ops_backend.report.firstpieceinspection.controller;

import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.ApproveFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.CreateFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.SubmitFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.response.FirstPieceInspectionResponse;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.service.FirstPieceInspectionService;
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
@RequestMapping(value={"/api/reports/first-piece-inspection"})
@Tag(name="First Piece Inspection", description="First piece inspection report APIs")
public class FirstPieceInspectionController {
    private final FirstPieceInspectionService firstPieceInspectionService;

    @PostMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Create first piece inspection report", description="Creates a new first piece inspection report in DRAFT status", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to create a first piece inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=CreateFirstPieceInspectionRequest.class), examples={@ExampleObject(name="CreateFirstPieceInspectionRequestExample", summary="Example payload to create a first piece inspection report", value="{\"reportDate\":\"2025-01-15\",\"shiftId\":1,\"lineId\":1,\"productCastingNumber\":\"CAST-001\",\"operatorName\":\"John Doe\",\"inspectorName\":\"Jane Smith\",\"remarks\":\"All measurements within tolerance\",\"entries\":[{\"parameterId\":1,\"observedValue\":\"12.5\",\"remark\":\"Within tolerance\"}]}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="First piece inspection report created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<FirstPieceInspectionResponse>> create(@Valid @RequestBody CreateFirstPieceInspectionRequest request) {
        FirstPieceInspectionResponse response = this.firstPieceInspectionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(new Object[]{response.getId()}).toUri();
        return ResponseEntity.created((URI)location).body(ApiResponse.<FirstPieceInspectionResponse>builder().success(true).message("First piece inspection report created successfully.").data(response).build());
    }

    @GetMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Fetch all first piece inspection reports", description="Returns a list of all first piece inspection reports. When pagination/filter params are provided returns a paginated PageResponse; otherwise the legacy full list.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="First piece inspection reports fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<?>> getAll(@org.springdoc.core.annotations.ParameterObject ReportFilterRequest filter) {
        if (filter.hasSearchCriteria()) {
            return ResponseEntity.ok(ApiResponse.<PageResponse<FirstPieceInspectionResponse>>builder().success(true).message("First piece inspection reports fetched successfully.").data(this.firstPieceInspectionService.search(filter)).build());
        }
        return ResponseEntity.ok(ApiResponse.<List<FirstPieceInspectionResponse>>builder().success(true).message("First piece inspection reports fetched successfully.").data(this.firstPieceInspectionService.getAll()).build());
    }

    @GetMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Fetch first piece inspection report by id", description="Returns a single first piece inspection report by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="First piece inspection report fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="First piece inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<FirstPieceInspectionResponse>> getById(@Parameter(description="ID of the first piece inspection report", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<FirstPieceInspectionResponse>builder().success(true).message("First piece inspection report fetched successfully.").data(this.firstPieceInspectionService.getById(id)).build());
    }

    @PostMapping(value={"/{id}/submit"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Submit first piece inspection report for approval", description="Submits a DRAFT first piece inspection report for approval", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to submit a first piece inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=SubmitFirstPieceInspectionRequest.class), examples={@ExampleObject(name="SubmitFirstPieceInspectionRequestExample", summary="Example payload to submit a first piece inspection report", value="{\"remarks\":\"Report is ready for review\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="First piece inspection report submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="First piece inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<FirstPieceInspectionResponse>> submit(@Parameter(description="ID of the first piece inspection report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody SubmitFirstPieceInspectionRequest request) {
        return ResponseEntity.ok(ApiResponse.<FirstPieceInspectionResponse>builder().success(true).message("First piece inspection report submitted successfully.").data(this.firstPieceInspectionService.submit(id, request)).build());
    }

    @PostMapping(value={"/{id}/approve"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Approve first piece inspection report", description="Approves a SUBMITTED first piece inspection report", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to approve a first piece inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=ApproveFirstPieceInspectionRequest.class), examples={@ExampleObject(name="ApproveFirstPieceInspectionRequestExample", summary="Example payload to approve a first piece inspection report", value="{\"remarks\":\"Approved - all checks passed\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="First piece inspection report approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="First piece inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<FirstPieceInspectionResponse>> approve(@Parameter(description="ID of the first piece inspection report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody ApproveFirstPieceInspectionRequest request) {
        return ResponseEntity.ok(ApiResponse.<FirstPieceInspectionResponse>builder().success(true).message("First piece inspection report approved successfully.").data(this.firstPieceInspectionService.approve(id, request)).build());
    }

    @PostMapping(value={"/{id}/reject"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Reject first piece inspection report", description="Rejects a SUBMITTED first piece inspection report", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to reject a first piece inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=ApproveFirstPieceInspectionRequest.class), examples={@ExampleObject(name="RejectFirstPieceInspectionRequestExample", summary="Example payload to reject a first piece inspection report", value="{\"remarks\":\"Rejected - corrective action required\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="First piece inspection report rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="First piece inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<FirstPieceInspectionResponse>> reject(@Parameter(description="ID of the first piece inspection report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody ApproveFirstPieceInspectionRequest request) {
        return ResponseEntity.ok(ApiResponse.<FirstPieceInspectionResponse>builder().success(true).message("First piece inspection report rejected successfully.").data(this.firstPieceInspectionService.reject(id, request)).build());
    }

    @DeleteMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Delete draft first piece inspection report", description="Deletes a DRAFT first piece inspection report by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="First piece inspection report deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="First piece inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Void> delete(@Parameter(description="ID of the first piece inspection report", example="1", required=true) @PathVariable Long id) {
        this.firstPieceInspectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public FirstPieceInspectionController(FirstPieceInspectionService firstPieceInspectionService) {
        this.firstPieceInspectionService = firstPieceInspectionService;
    }
}
