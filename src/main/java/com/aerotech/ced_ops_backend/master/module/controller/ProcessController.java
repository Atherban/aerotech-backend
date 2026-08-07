package com.aerotech.ced_ops_backend.master.module.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.master.module.dto.CreateProcessRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ProcessFilterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ProcessResponse;
import com.aerotech.ced_ops_backend.master.module.dto.UpdateProcessRequest;
import com.aerotech.ced_ops_backend.master.module.service.ProcessService;
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
@RequestMapping("/api/processes")
@Tag(name = "Process Master", description = "Process master data APIs within module template versions (Module-driven architecture)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService processService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a process", description = "Adds a process to a DRAFT template version and returns the created record.")
    public ResponseEntity<ApiResponse<ProcessResponse>> create(@Valid @RequestBody CreateProcessRequest request) {
        return ResponseEntity.ok(ApiResponse.<ProcessResponse>builder()
                .success(true)
                .message("Process created successfully.")
                .data(processService.create(request))
                .build());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get processes", description = "Fetches all processes. When pagination/filter params are provided returns a PageResponse; otherwise the full list.")
    public ResponseEntity<ApiResponse<?>> getAll(@ParameterObject ProcessFilterRequest filter) {
        if (filter.hasSearchCriteria()) {
            return ResponseEntity.ok(ApiResponse.<PageResponse<ProcessResponse>>builder()
                    .success(true)
                    .message("Processes fetched successfully.")
                    .data(processService.search(filter))
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<List<ProcessResponse>>builder()
                .success(true)
                .message("Processes fetched successfully.")
                .data(processService.getAll())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get process by ID", description = "Fetches a single process by its unique ID.")
    public ResponseEntity<ApiResponse<ProcessResponse>> getById(
            @Parameter(description = "ID of the process", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ProcessResponse>builder()
                .success(true)
                .message("Process fetched successfully.")
                .data(processService.getById(id))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a process", description = "Updates an existing process and returns the updated record.")
    public ResponseEntity<ApiResponse<ProcessResponse>> update(
            @Parameter(description = "ID of the process", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateProcessRequest request) {
        return ResponseEntity.ok(ApiResponse.<ProcessResponse>builder()
                .success(true)
                .message("Process updated successfully.")
                .data(processService.update(id, request))
                .build());
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Archive a process", description = "Marks a process ARCHIVED so it is not used in new reports but retained for historical ones.")
    public ResponseEntity<ApiResponse<ProcessResponse>> archive(
            @Parameter(description = "ID of the process", example = "1", required = true) @PathVariable Long id) {
        processService.archive(id);
        return ResponseEntity.ok(ApiResponse.<ProcessResponse>builder()
                .success(true)
                .message("Process archived successfully.")
                .data(processService.getById(id))
                .build());
    }

}