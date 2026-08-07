package com.aerotech.ced_ops_backend.master.module.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.master.module.dto.CreateParameterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ParameterFilterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ParameterResponse;
import com.aerotech.ced_ops_backend.master.module.dto.UpdateParameterRequest;
import com.aerotech.ced_ops_backend.master.module.service.ParameterCrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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

import java.util.List;

@RestController
@RequestMapping("/api/module-parameters")
@Tag(name = "Global Parameter Master", description = "Global reusable parameter master data APIs (Module-driven architecture)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ParameterController {

    private final ParameterCrudService parameterCrudService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a global parameter", description = "Creates a global reusable parameter and returns the created record.")
    public ResponseEntity<ApiResponse<ParameterResponse>> create(@Valid @RequestBody CreateParameterRequest request) {
        return ResponseEntity.ok(ApiResponse.<ParameterResponse>builder()
                .success(true)
                .message("Parameter created successfully.")
                .data(parameterCrudService.create(request))
                .build());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get global parameters", description = "Fetches all global parameters. When pagination/filter params are provided returns a PageResponse; otherwise the full list.")
    public ResponseEntity<ApiResponse<?>> getAll(@ParameterObject ParameterFilterRequest filter) {
        if (filter.hasSearchCriteria()) {
            return ResponseEntity.ok(ApiResponse.<PageResponse<ParameterResponse>>builder()
                    .success(true)
                    .message("Parameters fetched successfully.")
                    .data(parameterCrudService.search(filter))
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<List<ParameterResponse>>builder()
                .success(true)
                .message("Parameters fetched successfully.")
                .data(parameterCrudService.getAll())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a global parameter by ID", description = "Fetches a single global parameter by its unique ID.")
    public ResponseEntity<ApiResponse<ParameterResponse>> getById(
            @Parameter(description = "ID of the parameter", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ParameterResponse>builder()
                .success(true)
                .message("Parameter fetched successfully.")
                .data(parameterCrudService.getById(id))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a global parameter", description = "Updates an existing global parameter and returns the updated record.")
    public ResponseEntity<ApiResponse<ParameterResponse>> update(
            @Parameter(description = "ID of the parameter", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateParameterRequest request) {
        return ResponseEntity.ok(ApiResponse.<ParameterResponse>builder()
                .success(true)
                .message("Parameter updated successfully.")
                .data(parameterCrudService.update(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a global parameter (Super Admin only)", description = "Soft-deletes a global parameter by deactivating it.")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the parameter", example = "1", required = true) @PathVariable Long id) {
        parameterCrudService.delete(id);
        return ResponseEntity.noContent().build();
    }

}