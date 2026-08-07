package com.aerotech.ced_ops_backend.master.module.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.master.module.dto.CreateProcessParameterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ProcessParameterResponse;
import com.aerotech.ced_ops_backend.master.module.dto.UpdateProcessParameterRequest;
import com.aerotech.ced_ops_backend.master.module.service.ProcessParameterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/processes/{processId}/parameters")
@Tag(name = "Process Parameter Master", description = "Bindings of global parameters to processes (Module-driven architecture)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProcessParameterController {

    private final ProcessParameterService processParameterService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List a process's parameter bindings", description = "Fetches all parameter bindings of a process ordered by displayOrder.")
    public ResponseEntity<ApiResponse<List<ProcessParameterResponse>>> getByProcess(
            @Parameter(description = "ID of the process", example = "1", required = true) @PathVariable Long processId) {
        return ResponseEntity.ok(ApiResponse.<List<ProcessParameterResponse>>builder()
                .success(true)
                .message("Process parameters fetched successfully.")
                .data(processParameterService.getByProcess(processId))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Bind a parameter to a process", description = "Binds a global parameter to a process with the given ordering and constraints.")
    public ResponseEntity<ApiResponse<ProcessParameterResponse>> create(
            @Parameter(description = "ID of the process", example = "1", required = true) @PathVariable Long processId,
            @Valid @RequestBody CreateProcessParameterRequest request) {
        return ResponseEntity.ok(ApiResponse.<ProcessParameterResponse>builder()
                .success(true)
                .message("Process parameter created successfully.")
                .data(processParameterService.create(processId, request))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a process parameter binding by ID", description = "Fetches a single binding by its ID.")
    public ResponseEntity<ApiResponse<ProcessParameterResponse>> getById(
            @Parameter(description = "ID of the process", example = "1", required = true) @PathVariable Long processId,
            @Parameter(description = "ID of the binding", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ProcessParameterResponse>builder()
                .success(true)
                .message("Process parameter fetched successfully.")
                .data(processParameterService.getById(id))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a process parameter binding", description = "Updates the ordering and constraints of an existing binding.")
    public ResponseEntity<ApiResponse<ProcessParameterResponse>> update(
            @Parameter(description = "ID of the process", example = "1", required = true) @PathVariable Long processId,
            @Parameter(description = "ID of the binding", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateProcessParameterRequest request) {
        return ResponseEntity.ok(ApiResponse.<ProcessParameterResponse>builder()
                .success(true)
                .message("Process parameter updated successfully.")
                .data(processParameterService.update(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a process parameter binding (Super Admin only)", description = "Soft-deletes a binding by deactivating it.")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the process", example = "1", required = true) @PathVariable Long processId,
            @Parameter(description = "ID of the binding", example = "1", required = true) @PathVariable Long id) {
        processParameterService.delete(id);
        return ResponseEntity.noContent().build();
    }

}