package com.aerotech.ced_ops_backend.audit.controller;

import com.aerotech.ced_ops_backend.audit.dto.request.AuditFilterRequest;
import com.aerotech.ced_ops_backend.audit.dto.response.AuditLogResponse;
import com.aerotech.ced_ops_backend.audit.dto.response.AuditStatisticsResponse;
import com.aerotech.ced_ops_backend.audit.service.AuditService;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Trail", description = "Audit log management APIs (Admin / Super Admin only)")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get paginated audit logs with filtering and sorting")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getLogs(
            AuditFilterRequest filter
    ) {
        return ResponseEntity.ok(
                ApiResponse.<PageResponse<AuditLogResponse>>builder()
                        .success(true)
                        .message("Audit logs fetched successfully.")
                        .data(auditService.getLogs(filter))
                        .build()
        );
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get audit statistics (totals, counts by module and action)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit statistics fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<AuditStatisticsResponse>> getStatistics() {
        return ResponseEntity.ok(
                ApiResponse.<AuditStatisticsResponse>builder()
                        .success(true)
                        .message("Audit statistics fetched successfully.")
                        .data(auditService.getStatistics())
                        .build()
        );
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get the 10 most recent audit log entries")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recent activities fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getRecentActivities() {
        return ResponseEntity.ok(
                ApiResponse.<List<AuditLogResponse>>builder()
                        .success(true)
                        .message("Recent activities fetched successfully.")
                        .data(auditService.getRecentActivities())
                        .build()
        );
    }

}
