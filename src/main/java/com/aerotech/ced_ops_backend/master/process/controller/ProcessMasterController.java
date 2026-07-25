package com.aerotech.ced_ops_backend.master.process.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.master.process.dto.CreateProcessRequest;
import com.aerotech.ced_ops_backend.master.process.dto.ProcessResponse;
import com.aerotech.ced_ops_backend.master.process.dto.UpdateProcessRequest;
import com.aerotech.ced_ops_backend.master.process.service.ProcessMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/processes")
@RequiredArgsConstructor
@Tag(name = "Process Master", description = "Production process master data APIs")
public class ProcessMasterController {

    private final ProcessMasterService processService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a new process")
    public ResponseEntity<ApiResponse<ProcessResponse>> create(
            @Valid @RequestBody CreateProcessRequest request
    ) {
        ProcessResponse response = processService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<ProcessResponse>builder()
                        .success(true)
                        .message("Process created successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all processes")
    public ResponseEntity<ApiResponse<List<ProcessResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.<List<ProcessResponse>>builder()
                        .success(true)
                        .message("Processes fetched successfully.")
                        .data(processService.getAll())
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get process by ID")
    public ResponseEntity<ApiResponse<ProcessResponse>> getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ProcessResponse>builder()
                        .success(true)
                        .message("Process fetched successfully.")
                        .data(processService.getById(id))
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a process")
    public ResponseEntity<ApiResponse<ProcessResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProcessRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ProcessResponse>builder()
                        .success(true)
                        .message("Process updated successfully.")
                        .data(processService.update(id, request))
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a process (Super Admin only)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        processService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
