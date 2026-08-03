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
package com.aerotech.ced_ops_backend.report.predeliveryinspection.controller;

import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.ApprovePreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.CreatePreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.SubmitPreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.response.PreDeliveryInspectionResponse;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.service.PreDeliveryInspectionService;
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
@RequestMapping(value={"/api/reports/pre-delivery-inspection"})
@Tag(name="Pre-Delivery Inspection", description="Pre-delivery inspection report APIs")
public class PreDeliveryInspectionController {
    private final PreDeliveryInspectionService preDeliveryInspectionService;

    @PostMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Create pre-delivery inspection report", description="Creates a new pre-delivery inspection report in DRAFT status", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to create a pre-delivery inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=CreatePreDeliveryInspectionRequest.class), examples={@ExampleObject(name="CreatePreDeliveryInspectionRequestExample", summary="Example payload to create a pre-delivery inspection report", value="{\"reportDate\":\"2025-01-15\",\"shiftId\":1,\"lineId\":1,\"productPartNumber\":\"PART-001\",\"batchNumber\":\"BATCH-001\",\"inspectorName\":\"Jane Smith\",\"remarks\":\"All checks completed\",\"entries\":[{\"parameterId\":1,\"observedValue\":\"12.5\",\"remark\":\"Within tolerance\"}]}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="Pre-delivery inspection report created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<PreDeliveryInspectionResponse>> create(@Valid @RequestBody CreatePreDeliveryInspectionRequest request) {
        PreDeliveryInspectionResponse response = this.preDeliveryInspectionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(new Object[]{response.getId()}).toUri();
        return ResponseEntity.created((URI)location).body(ApiResponse.<PreDeliveryInspectionResponse>builder().success(true).message("Pre-delivery inspection report created successfully.").data(response).build());
    }

    @GetMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Fetch all pre-delivery inspection reports", description="Returns a list of all pre-delivery inspection reports. When pagination/filter params are provided returns a paginated PageResponse; otherwise the legacy full list.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Pre-delivery inspection reports fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<?>> getAll(@org.springdoc.core.annotations.ParameterObject ReportFilterRequest filter) {
        if (filter.hasSearchCriteria()) {
            return ResponseEntity.ok(ApiResponse.<PageResponse<PreDeliveryInspectionResponse>>builder().success(true).message("Pre-delivery inspection reports fetched successfully.").data(this.preDeliveryInspectionService.search(filter)).build());
        }
        return ResponseEntity.ok(ApiResponse.<List<PreDeliveryInspectionResponse>>builder().success(true).message("Pre-delivery inspection reports fetched successfully.").data(this.preDeliveryInspectionService.getAll()).build());
    }

    @GetMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Fetch pre-delivery inspection report by id", description="Returns a single pre-delivery inspection report by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Pre-delivery inspection report fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Pre-delivery inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<PreDeliveryInspectionResponse>> getById(@Parameter(description="ID of the pre-delivery inspection report", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<PreDeliveryInspectionResponse>builder().success(true).message("Pre-delivery inspection report fetched successfully.").data(this.preDeliveryInspectionService.getById(id)).build());
    }

    @PostMapping(value={"/{id}/submit"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Submit pre-delivery inspection report for approval", description="Submits a DRAFT pre-delivery inspection report for approval", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to submit a pre-delivery inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=SubmitPreDeliveryInspectionRequest.class), examples={@ExampleObject(name="SubmitPreDeliveryInspectionRequestExample", summary="Example payload to submit a pre-delivery inspection report", value="{\"remarks\":\"Report is ready for review\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Pre-delivery inspection report submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Pre-delivery inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<PreDeliveryInspectionResponse>> submit(@Parameter(description="ID of the pre-delivery inspection report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody SubmitPreDeliveryInspectionRequest request) {
        return ResponseEntity.ok(ApiResponse.<PreDeliveryInspectionResponse>builder().success(true).message("Pre-delivery inspection report submitted successfully.").data(this.preDeliveryInspectionService.submit(id, request)).build());
    }

    @PostMapping(value={"/{id}/approve"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Approve pre-delivery inspection report", description="Approves a SUBMITTED pre-delivery inspection report", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to approve a pre-delivery inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=ApprovePreDeliveryInspectionRequest.class), examples={@ExampleObject(name="ApprovePreDeliveryInspectionRequestExample", summary="Example payload to approve a pre-delivery inspection report", value="{\"remarks\":\"Approved - all checks passed\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Pre-delivery inspection report approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Pre-delivery inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<PreDeliveryInspectionResponse>> approve(@Parameter(description="ID of the pre-delivery inspection report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody ApprovePreDeliveryInspectionRequest request) {
        return ResponseEntity.ok(ApiResponse.<PreDeliveryInspectionResponse>builder().success(true).message("Pre-delivery inspection report approved successfully.").data(this.preDeliveryInspectionService.approve(id, request)).build());
    }

    @PostMapping(value={"/{id}/reject"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Reject pre-delivery inspection report", description="Rejects a SUBMITTED pre-delivery inspection report", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to reject a pre-delivery inspection report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=ApprovePreDeliveryInspectionRequest.class), examples={@ExampleObject(name="RejectPreDeliveryInspectionRequestExample", summary="Example payload to reject a pre-delivery inspection report", value="{\"remarks\":\"Rejected - corrective action required\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Pre-delivery inspection report rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Pre-delivery inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<PreDeliveryInspectionResponse>> reject(@Parameter(description="ID of the pre-delivery inspection report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody ApprovePreDeliveryInspectionRequest request) {
        return ResponseEntity.ok(ApiResponse.<PreDeliveryInspectionResponse>builder().success(true).message("Pre-delivery inspection report rejected successfully.").data(this.preDeliveryInspectionService.reject(id, request)).build());
    }

    @DeleteMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Delete draft pre-delivery inspection report", description="Deletes a DRAFT pre-delivery inspection report by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Pre-delivery inspection report deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Pre-delivery inspection report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Void> delete(@Parameter(description="ID of the pre-delivery inspection report", example="1", required=true) @PathVariable Long id) {
        this.preDeliveryInspectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public PreDeliveryInspectionController(PreDeliveryInspectionService preDeliveryInspectionService) {
        this.preDeliveryInspectionService = preDeliveryInspectionService;
    }
}
