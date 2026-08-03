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
package com.aerotech.ced_ops_backend.report.chemical.controller;

import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.ApproveChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.CreateChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.SubmitChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.response.ChemicalConsumptionResponse;
import com.aerotech.ced_ops_backend.report.chemical.service.ChemicalConsumptionService;
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
@RequestMapping(value={"/api/reports/chemical-consumption"})
@Tag(name="Chemical Consumption", description="Chemical consumption report APIs")
public class ChemicalConsumptionController {
    private final ChemicalConsumptionService chemicalConsumptionService;

    @PostMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Create chemical consumption report", description="Creates a new chemical consumption report in DRAFT status", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to create a chemical consumption report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=CreateChemicalConsumptionRequest.class), examples={@ExampleObject(name="CreateChemicalConsumptionRequestExample", summary="Example payload to create a chemical consumption report", value="{\"reportDate\":\"2025-01-15\",\"shiftId\":1,\"lineId\":1,\"remarks\":\"All chemicals consumed within limit\",\"entries\":[{\"parameterId\":1,\"observedValue\":\"25.5\",\"remark\":\"Within specification\"}]}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="Chemical consumption report created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ChemicalConsumptionResponse>> create(@Valid @RequestBody CreateChemicalConsumptionRequest request) {
        ChemicalConsumptionResponse response = this.chemicalConsumptionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(new Object[]{response.getId()}).toUri();
        return ResponseEntity.created((URI)location).body(ApiResponse.<ChemicalConsumptionResponse>builder().success(true).message("Chemical consumption report created successfully.").data(response).build());
    }

    @GetMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Fetch all chemical consumption reports", description="Returns a list of all chemical consumption reports. When pagination/filter params are provided returns a paginated PageResponse; otherwise the legacy full list.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Chemical consumption reports fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<?>> getAll(@org.springdoc.core.annotations.ParameterObject ReportFilterRequest filter) {
        if (filter.hasSearchCriteria()) {
            return ResponseEntity.ok(ApiResponse.<PageResponse<ChemicalConsumptionResponse>>builder().success(true).message("Chemical consumption reports fetched successfully.").data(this.chemicalConsumptionService.search(filter)).build());
        }
        return ResponseEntity.ok(ApiResponse.<List<ChemicalConsumptionResponse>>builder().success(true).message("Chemical consumption reports fetched successfully.").data(this.chemicalConsumptionService.getAll()).build());
    }

    @GetMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Fetch chemical consumption report by id", description="Returns a single chemical consumption report by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Chemical consumption report fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Chemical consumption report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ChemicalConsumptionResponse>> getById(@Parameter(description="ID of the chemical consumption report", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ChemicalConsumptionResponse>builder().success(true).message("Chemical consumption report fetched successfully.").data(this.chemicalConsumptionService.getById(id)).build());
    }

    @PostMapping(value={"/{id}/submit"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Submit chemical consumption report for approval", description="Submits a DRAFT chemical consumption report for approval", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to submit a chemical consumption report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=SubmitChemicalConsumptionRequest.class), examples={@ExampleObject(name="SubmitChemicalConsumptionRequestExample", summary="Example payload to submit a chemical consumption report", value="{\"remarks\":\"Ready for review\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Chemical consumption report submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Chemical consumption report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ChemicalConsumptionResponse>> submit(@Parameter(description="ID of the chemical consumption report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody SubmitChemicalConsumptionRequest request) {
        return ResponseEntity.ok(ApiResponse.<ChemicalConsumptionResponse>builder().success(true).message("Chemical consumption report submitted successfully.").data(this.chemicalConsumptionService.submit(id, request)).build());
    }

    @PostMapping(value={"/{id}/approve"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Approve chemical consumption report", description="Approves a SUBMITTED chemical consumption report", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to approve a chemical consumption report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=ApproveChemicalConsumptionRequest.class), examples={@ExampleObject(name="ApproveChemicalConsumptionRequestExample", summary="Example payload to approve a chemical consumption report", value="{\"remarks\":\"Approved - all consumption within specification\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Chemical consumption report approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Chemical consumption report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ChemicalConsumptionResponse>> approve(@Parameter(description="ID of the chemical consumption report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody ApproveChemicalConsumptionRequest request) {
        return ResponseEntity.ok(ApiResponse.<ChemicalConsumptionResponse>builder().success(true).message("Chemical consumption report approved successfully.").data(this.chemicalConsumptionService.approve(id, request)).build());
    }

    @PostMapping(value={"/{id}/reject"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Reject chemical consumption report", description="Rejects a SUBMITTED chemical consumption report", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to reject a chemical consumption report", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=ApproveChemicalConsumptionRequest.class), examples={@ExampleObject(name="RejectChemicalConsumptionRequestExample", summary="Example payload to reject a chemical consumption report", value="{\"remarks\":\"Rejected - values out of specification\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Chemical consumption report rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Chemical consumption report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ChemicalConsumptionResponse>> reject(@Parameter(description="ID of the chemical consumption report", example="1", required=true) @PathVariable Long id, @Valid @RequestBody ApproveChemicalConsumptionRequest request) {
        return ResponseEntity.ok(ApiResponse.<ChemicalConsumptionResponse>builder().success(true).message("Chemical consumption report rejected successfully.").data(this.chemicalConsumptionService.reject(id, request)).build());
    }

    @DeleteMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Delete draft chemical consumption report", description="Deletes a DRAFT chemical consumption report by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Chemical consumption report deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Chemical consumption report not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Void> delete(@Parameter(description="ID of the chemical consumption report", example="1", required=true) @PathVariable Long id) {
        this.chemicalConsumptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public ChemicalConsumptionController(ChemicalConsumptionService chemicalConsumptionService) {
        this.chemicalConsumptionService = chemicalConsumptionService;
    }
}
