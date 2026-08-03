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
package com.aerotech.ced_ops_backend.master.line.controller;

import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.master.line.dto.CreateLineRequest;
import com.aerotech.ced_ops_backend.master.line.dto.LineResponse;
import com.aerotech.ced_ops_backend.master.line.dto.UpdateLineRequest;
import com.aerotech.ced_ops_backend.master.line.service.LineService;
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
@RequestMapping(value={"/api/lines"})
@Tag(name="Line Master", description="Production line master data APIs")
public class LineController {
    private final LineService lineService;

    @PostMapping(produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Create a new production line", description="Creates a new production line with the provided details and returns the created line.", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Details of the production line to create", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=CreateLineRequest.class), examples={@ExampleObject(name="CreateLineRequest", summary="Example request to create a production line", value="{\n  \"name\": \"Assembly Line 1\",\n  \"description\": \"Main assembly line for product A\",\n  \"displayOrder\": 1\n}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="Production line created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - data constraint violation", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<LineResponse>> create(@Valid @RequestBody CreateLineRequest request) {
        LineResponse response = this.lineService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(new Object[]{response.getId()}).toUri();
        return ResponseEntity.created((URI)location).body(ApiResponse.<LineResponse>builder().success(true).message("Line created successfully.").data(response).build());
    }

    @GetMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get all production lines", description="Fetches all production lines ordered by display order.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Production lines fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<LineResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.<List<LineResponse>>builder().success(true).message("Lines fetched successfully.").data(this.lineService.getAll()).build());
    }

    @GetMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get production line by ID", description="Fetches a single production line by its unique ID.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Production line fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Production line not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<LineResponse>> getById(@Parameter(description="ID of the production line", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<LineResponse>builder().success(true).message("Line fetched successfully.").data(this.lineService.getById(id)).build());
    }

    @PutMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Update a production line", description="Updates an existing production line with the provided details and returns the updated line.", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Updated details of the production line", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=UpdateLineRequest.class), examples={@ExampleObject(name="UpdateLineRequest", summary="Example request to update a production line", value="{\n  \"name\": \"Assembly Line 1\",\n  \"description\": \"Main assembly line for product A\",\n  \"displayOrder\": 1,\n  \"active\": true\n}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Production line updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN or ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Production line not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - data constraint violation", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<LineResponse>> update(@Parameter(description="ID of the production line", example="1", required=true) @PathVariable Long id, @Valid @RequestBody UpdateLineRequest request) {
        return ResponseEntity.ok(ApiResponse.<LineResponse>builder().success(true).message("Line updated successfully.").data(this.lineService.update(id, request)).build());
    }

    @DeleteMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Delete a production line (Super Admin only)", description="Soft-deletes a production line by deactivating it. Only SUPER_ADMIN can perform this action.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Production line deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Production line not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Void> delete(@Parameter(description="ID of the production line", example="1", required=true) @PathVariable Long id) {
        this.lineService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public LineController(LineService lineService) {
        this.lineService = lineService;
    }
}
