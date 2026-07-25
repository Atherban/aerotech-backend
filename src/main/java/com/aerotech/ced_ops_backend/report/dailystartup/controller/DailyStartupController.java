package com.aerotech.ced_ops_backend.report.dailystartup.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.request.ApproveDailyStartupRequest;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.request.CreateDailyStartupRequest;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.request.SubmitDailyStartupRequest;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.response.DailyStartupResponse;
import com.aerotech.ced_ops_backend.report.dailystartup.service.DailyStartupService;
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
@RequestMapping("/api/reports/daily-startup")
@RequiredArgsConstructor
@Tag(name = "Daily Startup", description = "Daily startup checklist report APIs")
public class DailyStartupController {

    private final DailyStartupService dailyStartupService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create daily startup report")
    public ResponseEntity<ApiResponse<DailyStartupResponse>> create(
            @Valid @RequestBody CreateDailyStartupRequest request
    ) {
        DailyStartupResponse response = dailyStartupService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<DailyStartupResponse>builder()
                        .success(true)
                        .message("Daily startup report created successfully.")
                        .data(response)
                        .build()
        );

    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Fetch all daily startup reports")
    public ResponseEntity<ApiResponse<List<DailyStartupResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.<List<DailyStartupResponse>>builder()
                        .success(true)
                        .message("Daily startup reports fetched successfully.")
                        .data(dailyStartupService.getAll())
                        .build()
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Fetch daily startup report by id")
    public ResponseEntity<ApiResponse<DailyStartupResponse>> getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.<DailyStartupResponse>builder()
                        .success(true)
                        .message("Daily startup report fetched successfully.")
                        .data(dailyStartupService.getById(id))
                        .build()
        );

    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit daily startup report for approval")
    public ResponseEntity<ApiResponse<DailyStartupResponse>> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitDailyStartupRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<DailyStartupResponse>builder()
                        .success(true)
                        .message("Daily startup report submitted successfully.")
                        .data(dailyStartupService.submit(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Approve daily startup report")
    public ResponseEntity<ApiResponse<DailyStartupResponse>> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApproveDailyStartupRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<DailyStartupResponse>builder()
                        .success(true)
                        .message("Daily startup report approved successfully.")
                        .data(dailyStartupService.approve(id, request))
                        .build()
        );

    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Reject daily startup report")
    public ResponseEntity<ApiResponse<DailyStartupResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody ApproveDailyStartupRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.<DailyStartupResponse>builder()
                        .success(true)
                        .message("Daily startup report rejected successfully.")
                        .data(dailyStartupService.reject(id, request))
                        .build()
        );

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete draft daily startup report")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        dailyStartupService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
