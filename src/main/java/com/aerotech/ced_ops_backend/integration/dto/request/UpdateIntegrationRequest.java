package com.aerotech.ced_ops_backend.integration.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to update an existing integration")
public class UpdateIntegrationRequest {

    @NotBlank(message = "Integration name must not be blank")
    @Size(max = 200, message = "Integration name must not exceed 200 characters")
    @Schema(description = "Integration display name", example = "Production Webhook Updated")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Integration description", example = "Updated description")
    private String description;

    @NotBlank(message = "Configuration JSON must not be blank")
    @Schema(description = "Configuration JSON (use URL for webhook, credentials for others)")
    private String configJson;

    @Min(value = 0, message = "Retry count must be non-negative")
    @Schema(description = "Number of retry attempts", example = "5")
    private Integer retryCount;

    @Min(value = 1, message = "Timeout must be at least 1 second")
    @Schema(description = "Timeout in seconds", example = "60")
    private Integer timeoutSeconds;
}
