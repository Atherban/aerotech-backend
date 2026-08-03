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
 *  jakarta.validation.constraints.Min
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.security.core.Authentication
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.servlet.support.ServletUriComponentsBuilder
 */
package com.aerotech.ced_ops_backend.integration.controller;

import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.integration.dto.request.CreateIntegrationRequest;
import com.aerotech.ced_ops_backend.integration.dto.request.UpdateIntegrationRequest;
import com.aerotech.ced_ops_backend.integration.dto.response.IntegrationExecutionHistoryResponse;
import com.aerotech.ced_ops_backend.integration.dto.response.IntegrationResponse;
import com.aerotech.ced_ops_backend.integration.enums.IntegrationType;
import com.aerotech.ced_ops_backend.integration.service.IntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value={"/api/integrations"})
@Tag(name="Integration Center", description="Manage external system integrations")
@PreAuthorize(value="hasRole('SUPER_ADMIN')")
public class IntegrationController {
    private final IntegrationService integrationService;

    @GetMapping(produces={"application/json"})
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="List all integrations with optional filtering and pagination", description="Returns a paginated list of integrations, optionally filtered by type and search keyword, and sorted by the given field and direction")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Integrations fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<PageResponse<IntegrationResponse>>> getAll(@RequestParam(required=false) @Parameter(description="Filter by integration type", example="WEBHOOK") IntegrationType type, @RequestParam(required=false) @Parameter(description="Search by name or description", example="production") String search, @RequestParam(defaultValue="0") @Min(value=0L) @Parameter(description="Page number", example="0") @Min(value=0L) int page, @RequestParam(defaultValue="20") @Min(value=1L) @Parameter(description="Page size", example="20") @Min(value=1L) int size, @RequestParam(defaultValue="createdAt") @Parameter(description="Sort field", example="createdAt") String sortBy, @RequestParam(defaultValue="desc") @Parameter(description="Sort direction", example="desc") String sortDirection) {
        return ResponseEntity.ok(ApiResponse.<PageResponse<IntegrationResponse>>builder().success(true).message("Integrations fetched successfully.").data(this.integrationService.findAll(type, search, page, size, sortBy, sortDirection)).build());
    }

    @GetMapping(value={"/{id}"}, produces={"application/json"})
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get integration details by ID", description="Returns the details of a single integration by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Integration fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Integration not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<IntegrationResponse>> getById(@Parameter(description="Integration ID", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<IntegrationResponse>builder().success(true).message("Integration fetched successfully.").data(this.integrationService.findById(id)).build());
    }

    @PostMapping(produces={"application/json"})
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Create a new integration", description="Creates a new integration with the provided details and returns the created integration", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to create an integration", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=CreateIntegrationRequest.class), examples={@ExampleObject(name="CreateIntegrationRequestExample", summary="Example payload to create an integration", value="{\"name\":\"Production Webhook\",\"description\":\"Webhook for production line alerts\",\"type\":\"WEBHOOK\",\"configJson\":\"{\\\"url\\\":\\\"https://example.com/hook\\\"}\",\"retryCount\":3,\"timeoutSeconds\":30}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="Integration created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<IntegrationResponse>> create(@Valid @RequestBody CreateIntegrationRequest request, Authentication authentication) {
        String createdBy = authentication != null ? authentication.getName() : "SYSTEM";
        IntegrationResponse response = this.integrationService.create(request, createdBy);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(new Object[]{response.getId()}).toUri();
        return ResponseEntity.created((URI)location).body(ApiResponse.<IntegrationResponse>builder().success(true).message("Integration created successfully.").data(response).build());
    }

    @PutMapping(value={"/{id}"}, produces={"application/json"})
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Update an existing integration", description="Updates the details of an existing integration by its unique identifier", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to update an integration", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=UpdateIntegrationRequest.class), examples={@ExampleObject(name="UpdateIntegrationRequestExample", summary="Example payload to update an integration", value="{\"name\":\"Production Webhook Updated\",\"description\":\"Updated description\",\"configJson\":\"{\\\"url\\\":\\\"https://example.com/hook\\\"}\",\"retryCount\":5,\"timeoutSeconds\":60}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Integration updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Integration not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<IntegrationResponse>> update(@Parameter(description="Integration ID", example="1", required=true) @PathVariable Long id, @Valid @RequestBody UpdateIntegrationRequest request) {
        return ResponseEntity.ok(ApiResponse.<IntegrationResponse>builder().success(true).message("Integration updated successfully.").data(this.integrationService.update(id, request)).build());
    }

    @DeleteMapping(value={"/{id}"}, produces={"application/json"})
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Delete an integration", description="Deletes an integration by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Integration deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Integration not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Void> delete(@Parameter(description="Integration ID", example="1", required=true) @PathVariable Long id) {
        this.integrationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value={"/{id}/test"}, produces={"application/json"})
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Test integration connection", description="Runs a connection test against the integration and returns the updated integration with the test result")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Connection test completed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Integration not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<IntegrationResponse>> testConnection(@Parameter(description="Integration ID", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<IntegrationResponse>builder().success(true).message("Connection test completed.").data(this.integrationService.testConnection(id)).build());
    }

    @PostMapping(value={"/{id}/enable"}, produces={"application/json"})
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Enable an integration", description="Enables a disabled integration by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Integration enabled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Integration not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<IntegrationResponse>> enable(@Parameter(description="Integration ID", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<IntegrationResponse>builder().success(true).message("Integration enabled successfully.").data(this.integrationService.enable(id)).build());
    }

    @PostMapping(value={"/{id}/disable"}, produces={"application/json"})
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Disable an integration", description="Disables an active integration by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Integration disabled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Integration not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<IntegrationResponse>> disable(@Parameter(description="Integration ID", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<IntegrationResponse>builder().success(true).message("Integration disabled successfully.").data(this.integrationService.disable(id)).build());
    }

    @GetMapping(value={"/history"}, produces={"application/json"})
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get integration execution history with pagination", description="Returns a paginated list of integration execution history records, optionally filtered by integration ID")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Execution history fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<PageResponse<IntegrationExecutionHistoryResponse>>> getHistory(@RequestParam(required=false) @Parameter(description="Filter by integration ID", example="1") Long integrationId, @RequestParam(defaultValue="0") @Min(value=0L) @Parameter(description="Page number", example="0") @Min(value=0L) int page, @RequestParam(defaultValue="20") @Min(value=1L) @Parameter(description="Page size", example="20") @Min(value=1L) int size) {
        return ResponseEntity.ok(ApiResponse.<PageResponse<IntegrationExecutionHistoryResponse>>builder().success(true).message("Execution history fetched successfully.").data(this.integrationService.getHistory(integrationId, page, size)).build());
    }

    @Generated
    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }
}
