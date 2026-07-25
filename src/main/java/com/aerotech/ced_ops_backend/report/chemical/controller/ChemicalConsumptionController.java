package com.aerotech.ced_ops_backend.report.chemical.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.ApproveChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.CreateChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.SubmitChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.response.ChemicalConsumptionResponse;
import com.aerotech.ced_ops_backend.report.chemical.service.ChemicalConsumptionService;
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
@RequestMapping("/api/reports/chemical-consumption")
@RequiredArgsConstructor
@Tag(name = "Chemical Consumption", description = "Chemical consumption report APIs")
public class ChemicalConsumptionController {

    private final ChemicalConsumptionService chemicalConsumptionService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create chemical consumption report")
    public ResponseEntity<ApiResponse<ChemicalConsumptionResponse>> create(
            @Valid @RequestBody CreateChemicalConsumptionRequest request
    ) {
        ChemicalConsumptionResponse response = chemicalConsumptionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<ChemicalConsumptionResponse>builder()
                        .success(true)
                        .message("Chemical consumption report created successfully.")
                        .data(response)
                        .build()
        );

    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Fetch all chemical consumption reports")
    public ResponseEntity<ApiResponse<List<ChemicalConsumptionResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.<List<ChemicalConsumptionResponse>>builder()
                        .success(true)
                        .message("Chemical consumption reports fetched successfully.")
                        .data(chemicalConsumptionService.getAll())
                        .build()
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Fetch chemical consumption report by id")
    public ResponseEntity<ApiResponse<ChemicalConsumptionResponse>> getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ChemicalConsumptionResponse>builder()
                        .success(true)
                        .message("Chemical consumption report fetched successfully.")
                        .data(chemicalConsumptionService.getById(id))
                        .build()
        );

    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit chemical consumption report for approval")
    public ResponseEntity<ApiResponse<ChemicalConsumptionResponse>> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitChemicalConsumptionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ChemicalConsumptionResponse>builder()
                        .success(true)
                        .message("Chemical consumption report submitted successfully.")
                        .data(chemicalConsumptionService.submit(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Approve chemical consumption report")
    public ResponseEntity<ApiResponse<ChemicalConsumptionResponse>> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApproveChemicalConsumptionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ChemicalConsumptionResponse>builder()
                        .success(true)
                        .message("Chemical consumption report approved successfully.")
                        .data(chemicalConsumptionService.approve(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Reject chemical consumption report")
    public ResponseEntity<ApiResponse<ChemicalConsumptionResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody ApproveChemicalConsumptionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<ChemicalConsumptionResponse>builder()
                        .success(true)
                        .message("Chemical consumption report rejected successfully.")
                        .data(chemicalConsumptionService.reject(id, request))
                        .build()
        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete draft chemical consumption report")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        chemicalConsumptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
