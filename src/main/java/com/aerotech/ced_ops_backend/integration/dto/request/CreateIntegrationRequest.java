package com.aerotech.ced_ops_backend.integration.dto.request;

import com.aerotech.ced_ops_backend.integration.enums.IntegrationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to create a new integration")
public class CreateIntegrationRequest {

    @NotBlank(message = "Integration name is required")
    @Size(max = 200, message = "Integration name must not exceed 200 characters")
    @Schema(description = "Integration display name", example = "Production Webhook", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Integration description", example = "Webhook for production line alerts", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @NotNull(message = "Integration type is required")
    @Schema(description = "Integration type", example = "WEBHOOK", requiredMode = Schema.RequiredMode.REQUIRED)
    private IntegrationType type;

    @NotBlank(message = "Configuration JSON is required")
    @Schema(description = "Configuration JSON (use URL for webhook, credentials for others)", example = "{\"url\":\"https://example.com/hook\"}", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configJson;

    @Min(value = 0, message = "Retry count must be non-negative")
    @Schema(description = "Number of retry attempts", example = "3", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer retryCount;

    @Min(value = 1, message = "Timeout must be at least 1 second")
    @Schema(description = "Timeout in seconds", example = "30", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer timeoutSeconds;
}
