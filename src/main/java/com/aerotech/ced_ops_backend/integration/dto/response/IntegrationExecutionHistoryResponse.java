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

    @Schema(description = "Execution history ID", example = "1")
    private Long id;

    @Schema(description = "Integration ID", example = "1")
    private Long integrationId;

    @Schema(description = "Integration name", example = "Production Webhook")
    private String integrationName;

    @Schema(description = "Integration type", example = "WEBHOOK")
    private IntegrationType integrationType;

    @Schema(description = "Execution start time", example = "2025-06-15T10:30:00")
    private LocalDateTime startTime;

    @Schema(description = "Execution end time", example = "2025-06-15T10:30:05")
    private LocalDateTime endTime;

    @Schema(description = "Duration in milliseconds", example = "5000")
    private Long durationMs;

    @Schema(description = "Execution status", example = "SUCCESS")
    private IntegrationStatus status;

    @Schema(description = "Error message if the execution failed")
    private String errorMessage;

    @Schema(description = "Number of retry attempts", example = "0")
    private Integer retryCount;

    @Schema(description = "HTTP response code", example = "200")
    private String responseCode;

    @Schema(description = "Trigger type", example = "MANUAL")
    private String triggerType;

    @Schema(description = "Creation timestamp", example = "2025-06-15T10:30:00")
    private LocalDateTime createdAt;
}
