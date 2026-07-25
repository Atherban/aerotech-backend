package com.aerotech.ced_ops_backend.audit.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
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

    @Parameter(description = "User ID to filter by")
    @Schema(description = "User ID to filter by", example = "1")
    private Long userId;

    @Size(max = 100, message = "Module name must not exceed 100 characters")
    @Parameter(description = "Module name to filter by")
    @Schema(description = "Module name to filter by", example = "quality")
    private String module;

    @Size(max = 50, message = "Action name must not exceed 50 characters")
    @Parameter(description = "Action name to filter by")
    @Schema(description = "Action name to filter by", example = "CREATE")
    private String action;

    @Parameter(description = "Start date-time (ISO format)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Start date-time (ISO format)", example = "2025-01-01T00:00:00")
    private LocalDateTime dateFrom;

    @Parameter(description = "End date-time (ISO format)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "End date-time (ISO format)", example = "2025-12-31T23:59:59")
    private LocalDateTime dateTo;

    @Size(max = 50, message = "Sort field must not exceed 50 characters")
    @Parameter(description = "Sort field: timestamp, module, action, employeeId")
    @Schema(description = "Sort field: timestamp, module, action, employeeId", example = "timestamp")
    private String sortBy;

    @Size(max = 4, message = "Sort direction must be ASC or DESC")
    @Parameter(description = "Sort direction: ASC or DESC")
    @Schema(description = "Sort direction: ASC or DESC", example = "DESC")
    private String sortDirection;

    @Min(value = 0, message = "Page number must be non-negative")
    @Parameter(description = "Page number (zero-based)")
    @Schema(description = "Page number (zero-based)", example = "0")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Parameter(description = "Page size")
    @Schema(description = "Page size", example = "20")
    private int size = 20;

}
