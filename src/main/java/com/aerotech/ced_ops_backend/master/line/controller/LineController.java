package com.aerotech.ced_ops_backend.master.line.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.master.line.dto.CreateLineRequest;
import com.aerotech.ced_ops_backend.master.line.dto.LineResponse;
import com.aerotech.ced_ops_backend.master.line.dto.UpdateLineRequest;
import com.aerotech.ced_ops_backend.master.line.service.LineService;
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
@RequestMapping("/api/lines")
@RequiredArgsConstructor
@Tag(name = "Line Master", description = "Production line master data APIs")
public class LineController {

    private final LineService lineService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a new production line")
    public ResponseEntity<ApiResponse<LineResponse>> create(
            @Valid @RequestBody CreateLineRequest request
    ) {
        LineResponse response = lineService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<LineResponse>builder()
                        .success(true)
                        .message("Line created successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all production lines")
    public ResponseEntity<ApiResponse<List<LineResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.<List<LineResponse>>builder()
                        .success(true)
                        .message("Lines fetched successfully.")
                        .data(lineService.getAll())
                        .build()
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get production line by ID")
    public ResponseEntity<ApiResponse<LineResponse>> getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.<LineResponse>builder()
                        .success(true)
                        .message("Line fetched successfully.")
                        .data(lineService.getById(id))
                        .build()
        );

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a production line")
    public ResponseEntity<ApiResponse<LineResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLineRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<LineResponse>builder()
                        .success(true)
                        .message("Line updated successfully.")
                        .data(lineService.update(id, request))
                        .build()
        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a production line (Super Admin only)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        lineService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
