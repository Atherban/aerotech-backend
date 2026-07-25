package com.aerotech.ced_ops_backend.report.dailyinspection.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.ApproveDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.CreateDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.SubmitDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.response.DailyInspectionResponse;
import com.aerotech.ced_ops_backend.report.dailyinspection.service.DailyInspectionService;
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
@RequestMapping("/api/reports/daily-inspection")
@RequiredArgsConstructor
@Tag(name = "Daily Inspection", description = "Daily inspection report APIs")
public class DailyInspectionController {

    private final DailyInspectionService dailyInspectionService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create daily inspection report")
    public ResponseEntity<ApiResponse<DailyInspectionResponse>> create(
            @Valid @RequestBody CreateDailyInspectionRequest request
    ) {
        DailyInspectionResponse response = dailyInspectionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<DailyInspectionResponse>builder()
                        .success(true)
                        .message("Daily inspection report created successfully.")
                        .data(response)
                        .build()
        );

    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Fetch all daily inspection reports")
    public ResponseEntity<ApiResponse<List<DailyInspectionResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.<List<DailyInspectionResponse>>builder()
                        .success(true)
                        .message("Daily inspection reports fetched successfully.")
                        .data(dailyInspectionService.getAll())
                        .build()
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Fetch daily inspection report by id")
    public ResponseEntity<ApiResponse<DailyInspectionResponse>> getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.<DailyInspectionResponse>builder()
                        .success(true)
                        .message("Daily inspection report fetched successfully.")
                        .data(dailyInspectionService.getById(id))
                        .build()
        );

    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit daily inspection report for approval")
    public ResponseEntity<ApiResponse<DailyInspectionResponse>> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitDailyInspectionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<DailyInspectionResponse>builder()
                        .success(true)
                        .message("Daily inspection report submitted successfully.")
                        .data(dailyInspectionService.submit(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Approve daily inspection report")
    public ResponseEntity<ApiResponse<DailyInspectionResponse>> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApproveDailyInspectionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<DailyInspectionResponse>builder()
                        .success(true)
                        .message("Daily inspection report approved successfully.")
                        .data(dailyInspectionService.approve(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Reject daily inspection report")
    public ResponseEntity<ApiResponse<DailyInspectionResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody ApproveDailyInspectionRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<DailyInspectionResponse>builder()
                        .success(true)
                        .message("Daily inspection report rejected successfully.")
                        .data(dailyInspectionService.reject(id, request))
                        .build()
        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete draft daily inspection report")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        dailyInspectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
