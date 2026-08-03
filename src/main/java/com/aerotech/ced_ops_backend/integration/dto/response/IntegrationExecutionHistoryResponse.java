package com.aerotech.ced_ops_backend.integration.dto.response;

import com.aerotech.ced_ops_backend.integration.enums.IntegrationStatus;
import com.aerotech.ced_ops_backend.integration.enums.IntegrationType;
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
@Schema(description = "Response containing integration execution history details")
public class IntegrationExecutionHistoryResponse {

    @Schema(description = "Execution history ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Integration ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long integrationId;

    @Schema(description = "Integration name", example = "Production Webhook", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String integrationName;

    @Schema(description = "Integration type", example = "WEBHOOK", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private IntegrationType integrationType;

    @Schema(description = "Execution start time", example = "2025-06-15T10:30:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "Execution end time", example = "2025-06-15T10:30:05", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime endTime;

    @Schema(description = "Duration in milliseconds", example = "5000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long durationMs;

    @Schema(description = "Execution status", example = "SUCCESS", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private IntegrationStatus status;

    @Schema(description = "Error message if the execution failed", example = "Connection timeout", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String errorMessage;

    @Schema(description = "Number of retry attempts", example = "0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer retryCount;

    @Schema(description = "HTTP response code", example = "200", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String responseCode;

    @Schema(description = "Trigger type", example = "MANUAL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String triggerType;

    @Schema(description = "Creation timestamp", example = "2025-06-15T10:30:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime createdAt;
}
