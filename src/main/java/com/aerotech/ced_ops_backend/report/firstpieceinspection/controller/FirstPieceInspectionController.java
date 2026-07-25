package com.aerotech.ced_ops_backend.report.firstpieceinspection.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.ApproveFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.CreateFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.SubmitFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.response.FirstPieceInspectionResponse;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.service.FirstPieceInspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reports/first-piece-inspection")
@RequiredArgsConstructor
@Tag(name = "First Piece Inspection", description = "First piece inspection report APIs")
public class FirstPieceInspectionController {

    private final FirstPieceInspectionService firstPieceInspectionService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create first piece inspection report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "First piece inspection report created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<FirstPieceInspectionResponse>> create(
            @Valid @RequestBody CreateFirstPieceInspectionRequest request
    ) {
        FirstPieceInspectionResponse response = firstPieceInspectionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<FirstPieceInspectionResponse>builder()
                        .success(true)
                        .message("First piece inspection report created successfully.")
                        .data(response)
                        .build()
        );

    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Fetch all first piece inspection reports")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "First piece inspection reports fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<List<FirstPieceInspectionResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.<List<FirstPieceInspectionResponse>>builder()
                        .success(true)
                        .message("First piece inspection reports fetched successfully.")
                        .data(firstPieceInspectionService.getAll())
                        .build()
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Fetch first piece inspection report by id")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "First piece inspection report fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "First piece inspection report not found")
    })
    public ResponseEntity<ApiResponse<FirstPieceInspectionResponse>> getById(
            @io.swagger.v3.oas.annotations.Parameter(description = "First piece inspection report ID") @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.<FirstPieceInspectionResponse>builder()
                        .success(true)
                        .message("First piece inspection report fetched successfully.")
                        .data(firstPieceInspectionService.getById(id))
                        .build()
        );

    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Submit first piece inspection report for approval")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "First piece inspection report submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "First piece inspection report not found")
    })
    public ResponseEntity<ApiResponse<FirstPieceInspectionResponse>> submit(
            @io.swagger.v3.oas.annotations.Parameter(description = "First piece inspection report ID") @PathVariable Long id,
            @Valid @RequestBody SubmitFirstPieceInspectionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<FirstPieceInspectionResponse>builder()
                        .success(true)
                        .message("First piece inspection report submitted successfully.")
                        .data(firstPieceInspectionService.submit(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve first piece inspection report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "First piece inspection report approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN or ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "First piece inspection report not found")
    })
    public ResponseEntity<ApiResponse<FirstPieceInspectionResponse>> approve(
            @io.swagger.v3.oas.annotations.Parameter(description = "First piece inspection report ID") @PathVariable Long id,
            @Valid @RequestBody ApproveFirstPieceInspectionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<FirstPieceInspectionResponse>builder()
                        .success(true)
                        .message("First piece inspection report approved successfully.")
                        .data(firstPieceInspectionService.approve(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reject first piece inspection report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "First piece inspection report rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN or ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "First piece inspection report not found")
    })
    public ResponseEntity<ApiResponse<FirstPieceInspectionResponse>> reject(
            @io.swagger.v3.oas.annotations.Parameter(description = "First piece inspection report ID") @PathVariable Long id,
            @Valid @RequestBody ApproveFirstPieceInspectionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<FirstPieceInspectionResponse>builder()
                        .success(true)
                        .message("First piece inspection report rejected successfully.")
                        .data(firstPieceInspectionService.reject(id, request))
                        .build()
        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete draft first piece inspection report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "First piece inspection report deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "First piece inspection report not found")
    })
    public ResponseEntity<Void> delete(
            @io.swagger.v3.oas.annotations.Parameter(description = "First piece inspection report ID") @PathVariable Long id
    ) {
        firstPieceInspectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
