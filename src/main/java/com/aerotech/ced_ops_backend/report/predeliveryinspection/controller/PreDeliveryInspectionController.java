package com.aerotech.ced_ops_backend.report.predeliveryinspection.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.ApprovePreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.CreatePreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.SubmitPreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.response.PreDeliveryInspectionResponse;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.service.PreDeliveryInspectionService;
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
@RequestMapping("/api/reports/pre-delivery-inspection")
@RequiredArgsConstructor
@Tag(name = "Pre-Delivery Inspection", description = "Pre-delivery inspection report APIs")
public class PreDeliveryInspectionController {

    private final PreDeliveryInspectionService preDeliveryInspectionService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create pre-delivery inspection report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Pre-delivery inspection report created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<PreDeliveryInspectionResponse>> create(
            @Valid @RequestBody CreatePreDeliveryInspectionRequest request
    ) {
        PreDeliveryInspectionResponse response = preDeliveryInspectionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<PreDeliveryInspectionResponse>builder()
                        .success(true)
                        .message("Pre-delivery inspection report created successfully.")
                        .data(response)
                        .build()
        );

    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Fetch all pre-delivery inspection reports")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pre-delivery inspection reports fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    public ResponseEntity<ApiResponse<List<PreDeliveryInspectionResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.<List<PreDeliveryInspectionResponse>>builder()
                        .success(true)
                        .message("Pre-delivery inspection reports fetched successfully.")
                        .data(preDeliveryInspectionService.getAll())
                        .build()
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Fetch pre-delivery inspection report by id")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pre-delivery inspection report fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pre-delivery inspection report not found")
    })
    public ResponseEntity<ApiResponse<PreDeliveryInspectionResponse>> getById(
            @io.swagger.v3.oas.annotations.Parameter(description = "Pre-delivery inspection report ID") @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.<PreDeliveryInspectionResponse>builder()
                        .success(true)
                        .message("Pre-delivery inspection report fetched successfully.")
                        .data(preDeliveryInspectionService.getById(id))
                        .build()
        );

    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Submit pre-delivery inspection report for approval")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pre-delivery inspection report submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pre-delivery inspection report not found")
    })
    public ResponseEntity<ApiResponse<PreDeliveryInspectionResponse>> submit(
            @io.swagger.v3.oas.annotations.Parameter(description = "Pre-delivery inspection report ID") @PathVariable Long id,
            @Valid @RequestBody SubmitPreDeliveryInspectionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<PreDeliveryInspectionResponse>builder()
                        .success(true)
                        .message("Pre-delivery inspection report submitted successfully.")
                        .data(preDeliveryInspectionService.submit(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve pre-delivery inspection report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pre-delivery inspection report approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN or ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pre-delivery inspection report not found")
    })
    public ResponseEntity<ApiResponse<PreDeliveryInspectionResponse>> approve(
            @io.swagger.v3.oas.annotations.Parameter(description = "Pre-delivery inspection report ID") @PathVariable Long id,
            @Valid @RequestBody ApprovePreDeliveryInspectionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<PreDeliveryInspectionResponse>builder()
                        .success(true)
                        .message("Pre-delivery inspection report approved successfully.")
                        .data(preDeliveryInspectionService.approve(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reject pre-delivery inspection report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pre-delivery inspection report rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN or ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pre-delivery inspection report not found")
    })
    public ResponseEntity<ApiResponse<PreDeliveryInspectionResponse>> reject(
            @io.swagger.v3.oas.annotations.Parameter(description = "Pre-delivery inspection report ID") @PathVariable Long id,
            @Valid @RequestBody ApprovePreDeliveryInspectionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<PreDeliveryInspectionResponse>builder()
                        .success(true)
                        .message("Pre-delivery inspection report rejected successfully.")
                        .data(preDeliveryInspectionService.reject(id, request))
                        .build()
        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete draft pre-delivery inspection report")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Pre-delivery inspection report deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pre-delivery inspection report not found")
    })
    public ResponseEntity<Void> delete(
            @io.swagger.v3.oas.annotations.Parameter(description = "Pre-delivery inspection report ID") @PathVariable Long id
    ) {
        preDeliveryInspectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
