package com.aerotech.ced_ops_backend.integration.controller;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
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

import java.net.URI;

@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
@Tag(name = "Integration Center", description = "Manage external system integrations")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class IntegrationController {

    private final IntegrationService integrationService;

    @GetMapping
    @Operation(summary = "List all integrations with optional filtering and pagination")
    public ResponseEntity<ApiResponse<PageResponse<IntegrationResponse>>> getAll(
            @RequestParam(required = false) @Parameter(description = "Filter by integration type") IntegrationType type,
            @RequestParam(required = false) @Parameter(description = "Search by name or description") String search,
            @RequestParam(defaultValue = "0") @Min(0) @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Min(1) @Parameter(description = "Page size") int size,
            @RequestParam(defaultValue = "createdAt") @Parameter(description = "Sort field") String sortBy,
            @RequestParam(defaultValue = "desc") @Parameter(description = "Sort direction") String sortDirection) {
        return ResponseEntity.ok(
                ApiResponse.<PageResponse<IntegrationResponse>>builder()
                        .success(true)
                        .message("Integrations fetched successfully.")
                        .data(integrationService.findAll(type, search, page, size, sortBy, sortDirection))
                        .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get integration details by ID")
    public ResponseEntity<ApiResponse<IntegrationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.<IntegrationResponse>builder()
                        .success(true)
                        .message("Integration fetched successfully.")
                        .data(integrationService.findById(id))
                        .build());
    }

    @PostMapping
    @Operation(summary = "Create a new integration")
    public ResponseEntity<ApiResponse<IntegrationResponse>> create(
            @Valid @RequestBody CreateIntegrationRequest request,
            Authentication authentication) {
        String createdBy = authentication != null ? authentication.getName() : "SYSTEM";
        IntegrationResponse response = integrationService.create(request, createdBy);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<IntegrationResponse>builder()
                        .success(true)
                        .message("Integration created successfully.")
                        .data(response)
                        .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing integration")
    public ResponseEntity<ApiResponse<IntegrationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIntegrationRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<IntegrationResponse>builder()
                        .success(true)
                        .message("Integration updated successfully.")
                        .data(integrationService.update(id, request))
                        .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an integration")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        integrationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Test integration connection")
    public ResponseEntity<ApiResponse<IntegrationResponse>> testConnection(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.<IntegrationResponse>builder()
                        .success(true)
                        .message("Connection test completed.")
                        .data(integrationService.testConnection(id))
                        .build());
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "Enable an integration")
    public ResponseEntity<ApiResponse<IntegrationResponse>> enable(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.<IntegrationResponse>builder()
                        .success(true)
                        .message("Integration enabled successfully.")
                        .data(integrationService.enable(id))
                        .build());
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "Disable an integration")
    public ResponseEntity<ApiResponse<IntegrationResponse>> disable(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.<IntegrationResponse>builder()
                        .success(true)
                        .message("Integration disabled successfully.")
                        .data(integrationService.disable(id))
                        .build());
    }

    @GetMapping("/history")
    @Operation(summary = "Get integration execution history with pagination")
    public ResponseEntity<ApiResponse<PageResponse<IntegrationExecutionHistoryResponse>>> getHistory(
            @RequestParam(required = false) @Parameter(description = "Filter by integration ID") Long integrationId,
            @RequestParam(defaultValue = "0") @Min(0) @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Min(1) @Parameter(description = "Page size") int size) {
        return ResponseEntity.ok(
                ApiResponse.<PageResponse<IntegrationExecutionHistoryResponse>>builder()
                        .success(true)
                        .message("Execution history fetched successfully.")
                        .data(integrationService.getHistory(integrationId, page, size))
                        .build());
    }
}
