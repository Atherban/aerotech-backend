package com.aerotech.ced_ops_backend.audit.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing a single audit log entry")
public class AuditLogResponse {

    @Schema(description = "Audit log ID", example = "1")
    private Long id;

    @Schema(description = "Timestamp of the audit event", example = "2025-06-15T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "User ID who performed the action", example = "1")
    private Long userId;

    @Schema(description = "Employee ID", example = "EMP-001")
    private String employeeId;

    @Schema(description = "Username", example = "jdoe")
    private String username;

    @Schema(description = "User role", example = "OPERATOR")
    private String userRole;

    @Schema(description = "Module name", example = "quality")
    private String module;

    @Schema(description = "Entity type", example = "Report")
    private String entityType;

    @Schema(description = "Entity ID", example = "REP-001234")
    private String entityId;

    @Schema(description = "Action performed", example = "CREATE")
    private String action;

    @Schema(description = "Previous value before the change (JSON)")
    private String previousValue;

    @Schema(description = "New value after the change (JSON)")
    private String newValue;

    @Schema(description = "IP address of the requester", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "User agent string", example = "Mozilla/5.0")
    private String userAgent;

    @Schema(description = "Additional metadata (JSON)")
    private String metadata;

}
