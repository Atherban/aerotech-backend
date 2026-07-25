package com.aerotech.ced_ops_backend.master.parameter.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.master.parameter.dto.CreateParameterRequest;
import com.aerotech.ced_ops_backend.master.parameter.dto.ParameterResponse;
import com.aerotech.ced_ops_backend.master.parameter.dto.UpdateParameterRequest;
import com.aerotech.ced_ops_backend.master.parameter.service.ParameterMasterService;
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
@RequestMapping("/api/parameters")
@RequiredArgsConstructor
@Tag(name = "Parameter Master", description = "Inspection parameter master data APIs")
public class ParameterMasterController {

    private final ParameterMasterService parameterService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a new inspection parameter")
    public ResponseEntity<ApiResponse<ParameterResponse>> create(
            @Valid @RequestBody CreateParameterRequest request) {
        ParameterResponse response = parameterService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<ParameterResponse>builder()
                        .success(true)
                        .message("Parameter created successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all parameters")
    public ResponseEntity<ApiResponse<List<ParameterResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.<List<ParameterResponse>>builder()
                        .success(true)
                        .message("Parameters fetched successfully.")
                        .data(parameterService.getAll())
                        .build()
        );
    }

    @GetMapping("/process/{processId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get parameters by process ID")
    public ResponseEntity<ApiResponse<List<ParameterResponse>>> getByProcess(
            @PathVariable Long processId) {

        return ResponseEntity.ok(
                ApiResponse.<List<ParameterResponse>>builder()
                        .success(true)
                        .message("Parameters fetched successfully.")
                        .data(parameterService.getByProcess(processId))
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get parameter by ID")
    public ResponseEntity<ApiResponse<ParameterResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.<ParameterResponse>builder()
                        .success(true)
                        .message("Parameter fetched successfully.")
                        .data(parameterService.getById(id))
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update an inspection parameter")
    public ResponseEntity<ApiResponse<ParameterResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateParameterRequest request) {

        return ResponseEntity.ok(
                ApiResponse.<ParameterResponse>builder()
                        .success(true)
                        .message("Parameter updated successfully.")
                        .data(parameterService.update(id, request))
                        .build()
        );
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a parameter (Super Admin only)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {
        parameterService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
