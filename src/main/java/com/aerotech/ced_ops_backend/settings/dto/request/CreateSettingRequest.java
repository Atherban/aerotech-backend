package com.aerotech.ced_ops_backend.settings.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to create a new system setting")
public class CreateSettingRequest {

    @NotBlank(message = "Setting key is required")
    @Schema(description = "Unique setting key", example = "app.maintenance_mode", requiredMode = Schema.RequiredMode.REQUIRED)
    private String settingKey;

    @NotBlank(message = "Setting value is required")
    @Schema(description = "Setting value", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private String settingValue;

    @NotNull(message = "Category is required")
    @Schema(description = "Setting category", example = "GENERAL", allowableValues = {"GENERAL", "REPORT_SETTINGS", "NOTIFICATION_SETTINGS", "ATTACHMENT_SETTINGS", "SECURITY_SETTINGS", "DASHBOARD_SETTINGS"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    @NotBlank(message = "Data type is required")
    @Schema(description = "Data type", example = "BOOLEAN", allowableValues = {"STRING", "INTEGER", "LONG", "BOOLEAN", "DECIMAL", "JSON"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String dataType;

    @jakarta.validation.constraints.Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Description of the setting", example = "Enable or disable maintenance mode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

}
