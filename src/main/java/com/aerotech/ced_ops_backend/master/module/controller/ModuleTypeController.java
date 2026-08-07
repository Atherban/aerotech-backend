package com.aerotech.ced_ops_backend.master.module.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.master.module.dto.CreateModuleTypeRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ModuleTypeFilterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ModuleTypeResponse;
import com.aerotech.ced_ops_backend.master.module.dto.UpdateModuleTypeRequest;
import com.aerotech.ced_ops_backend.master.module.service.ModuleTypeService;
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
@RequestMapping("/api/module-types")
@Tag(name = "Module Type Master", description = "Configurable module type master data APIs (Module-driven architecture)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ModuleTypeController {

    private final ModuleTypeService moduleTypeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a module type", description = "Creates a new module type and returns the created record.")
    public ResponseEntity<ApiResponse<ModuleTypeResponse>> create(@Valid @RequestBody CreateModuleTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.<ModuleTypeResponse>builder()
                .success(true)
                .message("Module type created successfully.")
                .data(moduleTypeService.create(request))
                .build());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get module types", description = "Fetches all module types. When pagination/filter params are provided returns a PageResponse; otherwise the full list.")
    public ResponseEntity<ApiResponse<?>> getAll(@ParameterObject ModuleTypeFilterRequest filter) {
        if (filter.hasSearchCriteria()) {
            return ResponseEntity.ok(ApiResponse.<PageResponse<ModuleTypeResponse>>builder()
                    .success(true)
                    .message("Module types fetched successfully.")
                    .data(moduleTypeService.search(filter))
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<List<ModuleTypeResponse>>builder()
                .success(true)
                .message("Module types fetched successfully.")
                .data(moduleTypeService.getAll())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get module type by ID", description = "Fetches a single module type by its unique ID.")
    public ResponseEntity<ApiResponse<ModuleTypeResponse>> getById(
            @Parameter(description = "ID of the module type", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ModuleTypeResponse>builder()
                .success(true)
                .message("Module type fetched successfully.")
                .data(moduleTypeService.getById(id))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a module type", description = "Updates an existing module type and returns the updated record.")
    public ResponseEntity<ApiResponse<ModuleTypeResponse>> update(
            @Parameter(description = "ID of the module type", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateModuleTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.<ModuleTypeResponse>builder()
                .success(true)
                .message("Module type updated successfully.")
                .data(moduleTypeService.update(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Deactivate a module type (Super Admin only)", description = "Soft-deletes a module type by deactivating it.")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the module type", example = "1", required = true) @PathVariable Long id) {
        moduleTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

}