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
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.servlet.support.ServletUriComponentsBuilder
 */
package com.aerotech.ced_ops_backend.master.parameter.controller;

import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.master.parameter.dto.CreateParameterRequest;
import com.aerotech.ced_ops_backend.master.parameter.dto.ParameterFilterRequest;
import com.aerotech.ced_ops_backend.master.parameter.dto.ParameterResponse;
import com.aerotech.ced_ops_backend.master.parameter.dto.UpdateParameterRequest;
import com.aerotech.ced_ops_backend.master.parameter.service.ParameterMasterService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value={"/api/parameters"})
@Tag(name="Parameter Master", description="Inspection parameter master data APIs")
public class ParameterMasterController {
    private final ParameterMasterService parameterService;

    @PostMapping(produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Create a new inspection parameter", description="Creates a new inspection parameter for a report type template and returns the created parameter.", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Details of the inspection parameter to create", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=CreateParameterRequest.class), examples={@ExampleObject(name="CreateParameterRequest", summary="Example request to create an inspection parameter", value="{\n  \"reportType\": \"CHEMICAL_CONSUMPTION\",\n  \"parameterName\": \"Bath Temperature\",\n  \"minValue\": 20.0,\n  \"maxValue\": 40.0,\n  \"unit\": \"\u00b0C\",\n  \"testMethod\": \"Thermometer\",\n  \"frequency\": \"EVERY_SHIFT\",\n  \"inputType\": \"NUMBER\",\n  \"mandatory\": true,\n  \"visible\": true,\n  \"defaultValue\": \"25.0\",\n  \"displayOrder\": 1\n}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="Inspection parameter created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - data constraint violation", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ParameterResponse>> create(@Valid @RequestBody CreateParameterRequest request) {
        ParameterResponse response = this.parameterService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(new Object[]{response.getId()}).toUri();
        return ResponseEntity.created((URI)location).body(ApiResponse.<ParameterResponse>builder().success(true).message("Parameter created successfully.").data(response).build());
    }

    @GetMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get all parameters", description="Fetches all inspection parameters configured in the system. When pagination/filter params are provided returns a paginated PageResponse; otherwise the legacy full list.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Parameters fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<?>> getAll(@org.springdoc.core.annotations.ParameterObject ParameterFilterRequest filter) {
        if (filter.hasSearchCriteria()) {
            return ResponseEntity.ok(ApiResponse.<PageResponse<ParameterResponse>>builder().success(true).message("Parameters fetched successfully.").data(this.parameterService.search(filter)).build());
        }
        return ResponseEntity.ok(ApiResponse.<List<ParameterResponse>>builder().success(true).message("Parameters fetched successfully.").data(this.parameterService.getAll()).build());
    }

    @GetMapping(value={"/report-type/{reportType}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get parameters by report type (the configured template)", description="Fetches all inspection parameters configured for the given report type template.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Parameters fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - invalid report type value", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<ParameterResponse>>> getByReportType(@Parameter(description="Report type (template) to filter parameters by", example="CHEMICAL_CONSUMPTION", required=true) @PathVariable ReportType reportType) {
        return ResponseEntity.ok(ApiResponse.<List<ParameterResponse>>builder().success(true).message("Parameters fetched successfully.").data(this.parameterService.getByReportType(reportType)).build());
    }

    @GetMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get parameter by ID", description="Fetches a single inspection parameter by its unique ID.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Parameter fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Parameter not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ParameterResponse>> getById(@Parameter(description="ID of the inspection parameter", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ParameterResponse>builder().success(true).message("Parameter fetched successfully.").data(this.parameterService.getById(id)).build());
    }

    @PutMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Update an inspection parameter", description="Updates an existing inspection parameter with the provided details and returns the updated parameter.", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Updated details of the inspection parameter", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=UpdateParameterRequest.class), examples={@ExampleObject(name="UpdateParameterRequest", summary="Example request to update an inspection parameter", value="{\n  \"parameterName\": \"Temperature\",\n  \"minValue\": 20.0,\n  \"maxValue\": 100.0,\n  \"unit\": \"\u00b0C\",\n  \"testMethod\": \"Visual Inspection\",\n  \"frequency\": \"HOURLY\",\n  \"inputType\": \"NUMBER\",\n  \"mandatory\": true,\n  \"visible\": true,\n  \"defaultValue\": \"25.0\",\n  \"displayOrder\": 1,\n  \"active\": true\n}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Parameter updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Parameter not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - data constraint violation", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<ParameterResponse>> update(@Parameter(description="ID of the inspection parameter", example="1", required=true) @PathVariable Long id, @Valid @RequestBody UpdateParameterRequest request) {
        return ResponseEntity.ok(ApiResponse.<ParameterResponse>builder().success(true).message("Parameter updated successfully.").data(this.parameterService.update(id, request)).build());
    }

    @DeleteMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Delete a parameter (Super Admin only)", description="Soft-deletes an inspection parameter by deactivating it. Only SUPER_ADMIN can perform this action.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Parameter deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Parameter not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Void> delete(@Parameter(description="ID of the inspection parameter", example="1", required=true) @PathVariable Long id) {
        this.parameterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public ParameterMasterController(ParameterMasterService parameterService) {
        this.parameterService = parameterService;
    }
}
