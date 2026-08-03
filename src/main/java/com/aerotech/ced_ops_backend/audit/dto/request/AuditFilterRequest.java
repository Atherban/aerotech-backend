package com.aerotech.ced_ops_backend.audit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Request to filter audit log entries")
public class AuditFilterRequest {

    @Schema(description = "User ID to filter by", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long userId;

    @Size(max = 100, message = "Module name must not exceed 100 characters")
    @Schema(description = "Module name to filter by", example = "PROCESS_MONITORING", allowableValues = {"AUTHENTICATION", "USER_MANAGEMENT", "SHIFT_MASTER", "LINE_MASTER", "PROCESS_MASTER", "PARAMETER_MASTER", "PROCESS_MONITORING", "CHEMICAL_CONSUMPTION", "DAILY_STARTUP", "FIRST_PIECE_INSPECTION", "DAILY_INSPECTION", "PRE_DELIVERY_INSPECTION", "SYSTEM", "DASHBOARD", "APPROVAL_CENTER"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String module;

    @Size(max = 50, message = "Action name must not exceed 50 characters")
    @Schema(description = "Action name to filter by", example = "CREATE", allowableValues = {"LOGIN", "LOGOUT", "FAILED_LOGIN", "PASSWORD_CHANGE", "TOKEN_REFRESH", "CREATE", "UPDATE", "DELETE", "ACTIVATE", "DEACTIVATE", "ROLE_CHANGE", "DRAFT_SAVED", "SUBMIT", "APPROVE", "REJECT", "CANCEL", "ATTACHMENT_UPLOAD", "ATTACHMENT_DELETE"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String action;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Start date-time (ISO format)", example = "2025-01-01T00:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "End date-time (ISO format)", example = "2025-12-31T23:59:59", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime dateTo;

    @Size(max = 50, message = "Sort field must not exceed 50 characters")
    @Schema(description = "Sort field: timestamp, module, action, employeeId", example = "timestamp", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sortBy;

    @Size(max = 4, message = "Sort direction must be ASC or DESC")
    @Schema(description = "Sort direction: ASC or DESC", example = "DESC", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sortDirection;

    @Min(value = 0, message = "Page number must be non-negative")
    @Schema(description = "Page number (zero-based)", example = "0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Schema(description = "Page size", example = "20", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int size = 20;

}
