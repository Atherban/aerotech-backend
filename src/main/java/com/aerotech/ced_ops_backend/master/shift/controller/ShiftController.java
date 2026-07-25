package com.aerotech.ced_ops_backend.master.shift.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.master.shift.dto.CreateShiftRequest;
import com.aerotech.ced_ops_backend.master.shift.dto.ShiftResponse;
import com.aerotech.ced_ops_backend.master.shift.dto.UpdateShiftRequest;
import com.aerotech.ced_ops_backend.master.shift.service.ShiftService;
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
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
@Tag(name = "Shift Master", description = "Production shift master data APIs")
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a new shift")
    public ResponseEntity<ApiResponse<ShiftResponse>> create(
            @Valid @RequestBody CreateShiftRequest request
    ) {
        ShiftResponse response = shiftService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<ShiftResponse>builder()
                        .success(true)
                        .message("Shift created successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all shifts")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.<List<ShiftResponse>>builder()
                        .success(true)
                        .message("Shifts fetched successfully.")
                        .data(shiftService.getAll())
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get shift by ID")
    public ResponseEntity<ApiResponse<ShiftResponse>> getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ShiftResponse>builder()
                        .success(true)
                        .message("Shift fetched successfully.")
                        .data(shiftService.getById(id))
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a shift")
    public ResponseEntity<ApiResponse<ShiftResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShiftRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ShiftResponse>builder()
                        .success(true)
                        .message("Shift updated successfully.")
                        .data(shiftService.update(id, request))
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a shift (Super Admin only)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        shiftService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
