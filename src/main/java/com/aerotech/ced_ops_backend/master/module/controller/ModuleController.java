package com.aerotech.ced_ops_backend.master.module.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.master.module.dto.CreateModuleRequest;
import com.aerotech.ced_ops_backend.master.module.dto.CreateTemplateVersionRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ModuleFilterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ModuleResponse;
import com.aerotech.ced_ops_backend.master.module.dto.ProcessParameterResponse;
import com.aerotech.ced_ops_backend.master.module.dto.ProcessResponse;
import com.aerotech.ced_ops_backend.master.module.dto.TemplateVersionResponse;
import com.aerotech.ced_ops_backend.master.module.dto.UpdateModuleRequest;
import com.aerotech.ced_ops_backend.master.module.service.ModuleService;
import com.aerotech.ced_ops_backend.master.module.service.TemplateVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@Tag(name = "Module Master", description = "Module (reusable report template) master data APIs (Module-driven architecture)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;
    private final TemplateVersionService templateVersionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a module", description = "Creates a module in DRAFT status together with its initial template version.")
    public ResponseEntity<ApiResponse<ModuleResponse>> create(@Valid @RequestBody CreateModuleRequest request) {
        return ResponseEntity.ok(ApiResponse.<ModuleResponse>builder()
                .success(true)
                .message("Module created successfully.")
                .data(moduleService.create(request))
                .build());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get modules", description = "Fetches all modules. When pagination/filter params are provided returns a PageResponse; otherwise the full list.")
    public ResponseEntity<ApiResponse<?>> getAll(@ParameterObject ModuleFilterRequest filter) {
        if (filter.hasSearchCriteria()) {
            return ResponseEntity.ok(ApiResponse.<PageResponse<ModuleResponse>>builder()
                    .success(true)
                    .message("Modules fetched successfully.")
                    .data(moduleService.search(filter))
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<List<ModuleResponse>>builder()
                .success(true)
                .message("Modules fetched successfully.")
                .data(moduleService.getAll())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get module by ID", description = "Fetches a single module by its unique ID.")
    public ResponseEntity<ApiResponse<ModuleResponse>> getById(
            @Parameter(description = "ID of the module", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ModuleResponse>builder()
                .success(true)
                .message("Module fetched successfully.")
                .data(moduleService.getById(id))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a module", description = "Updates module header metadata and returns the updated module.")
    public ResponseEntity<ApiResponse<ModuleResponse>> update(
            @Parameter(description = "ID of the module", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateModuleRequest request) {
        return ResponseEntity.ok(ApiResponse.<ModuleResponse>builder()
                .success(true)
                .message("Module updated successfully.")
                .data(moduleService.update(id, request))
                .build());
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Archive a module (Super Admin only)", description = "Archives an ACTIVE module so no new reports can be created against it.")
    public ResponseEntity<ApiResponse<ModuleResponse>> archive(
            @Parameter(description = "ID of the module", example = "1", required = true) @PathVariable Long id) {
        moduleService.archive(id);
        return ResponseEntity.ok(ApiResponse.<ModuleResponse>builder()
                .success(true)
                .message("Module archived successfully.")
                .data(moduleService.getById(id))
                .build());
    }

    @GetMapping("/{id}/versions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List template versions of a module", description = "Fetches all template versions of a module, newest first.")
    public ResponseEntity<ApiResponse<List<TemplateVersionResponse>>> getVersions(
            @Parameter(description = "ID of the module", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<List<TemplateVersionResponse>>builder()
                .success(true)
                .message("Template versions fetched successfully.")
                .data(templateVersionService.getVersions(id))
                .build());
    }

    @PostMapping("/{id}/versions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a new template version", description = "Snapshots the current ACTIVE version (processes and bindings) into a new DRAFT version.")
    public ResponseEntity<ApiResponse<TemplateVersionResponse>> createVersion(
            @Parameter(description = "ID of the module", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody CreateTemplateVersionRequest request) {
        return ResponseEntity.ok(ApiResponse.<TemplateVersionResponse>builder()
                .success(true)
                .message("Template version created successfully.")
                .data(templateVersionService.createVersion(id, request))
                .build());
    }

    @PostMapping("/{id}/versions/{versionId}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Publish a template version", description = "Activates the DRAFT version, superseding any prior ACTIVE version, and activates the module.")
    public ResponseEntity<ApiResponse<TemplateVersionResponse>> publishVersion(
            @Parameter(description = "ID of the module", example = "1", required = true) @PathVariable Long id,
            @Parameter(description = "ID of the template version to publish", example = "1", required = true) @PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.<TemplateVersionResponse>builder()
                .success(true)
                .message("Template version published successfully.")
                .data(templateVersionService.publish(id, versionId))
                .build());
    }

    @GetMapping("/{id}/versions/{versionId}/processes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List processes of a template version", description = "Fetches all processes of a template version ordered by displayOrder.")
    public ResponseEntity<ApiResponse<List<ProcessResponse>>> getProcesses(
            @Parameter(description = "ID of the module", example = "1", required = true) @PathVariable Long id,
            @Parameter(description = "ID of the template version", example = "1", required = true) @PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.<List<ProcessResponse>>builder()
                .success(true)
                .message("Processes fetched successfully.")
                .data(templateVersionService.getProcesses(versionId))
                .build());
    }

    @GetMapping("/{id}/versions/{versionId}/process-parameters")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all process parameter bindings of a template version", description = "Fetches every parameter binding across all processes of the version, ordered by displayOrder.")
    public ResponseEntity<ApiResponse<List<ProcessParameterResponse>>> getProcessParameters(
            @Parameter(description = "ID of the module", example = "1", required = true) @PathVariable Long id,
            @Parameter(description = "ID of the template version", example = "1", required = true) @PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.<List<ProcessParameterResponse>>builder()
                .success(true)
                .message("Process parameters fetched successfully.")
                .data(templateVersionService.getProcessParameters(versionId))
                .build());
    }

}