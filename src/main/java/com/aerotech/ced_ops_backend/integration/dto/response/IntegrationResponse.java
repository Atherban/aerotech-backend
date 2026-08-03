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
@Schema(description = "Response containing integration details")
public class IntegrationResponse {

    @Schema(description = "Integration ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Integration display name", example = "Production Webhook", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    @Schema(description = "Integration description", example = "Webhook for production line alerts", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "Integration type", example = "WEBHOOK", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private IntegrationType type;

    @Schema(description = "Integration status", example = "ACTIVE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private IntegrationStatus status;

    @Schema(description = "Configuration JSON", example = "{\"url\":\"https://example.com/hook\"}", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String configJson;

    @Schema(description = "Number of retry attempts", example = "3", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer retryCount;

    @Schema(description = "Timeout in seconds", example = "30", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer timeoutSeconds;

    @Schema(description = "Whether the integration is active", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isActive;

    @Schema(description = "Timestamp of last test", example = "2025-06-15T10:30:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime lastTestedAt;

    @Schema(description = "Status of the last test", example = "SUCCESS", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String lastTestStatus;

    @Schema(description = "Username who created the integration", example = "admin", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String createdBy;

    @Schema(description = "Creation timestamp", example = "2025-06-15T10:30:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-06-15T10:30:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime updatedAt;
}
