package com.aerotech.ced_ops_backend.settings.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
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
    @Parameter(description = "Unique setting key")
    @Schema(description = "Unique setting key", example = "app.maintenance_mode")
    private String settingKey;

    @NotBlank(message = "Setting value is required")
    @Parameter(description = "Setting value")
    @Schema(description = "Setting value", example = "false")
    private String settingValue;

    @NotNull(message = "Category is required")
    @Parameter(description = "Setting category")
    @Schema(description = "Setting category", example = "general")
    private String category;

    @NotBlank(message = "Data type is required")
    @Parameter(description = "Data type: STRING, INTEGER, LONG, BOOLEAN, DECIMAL, JSON")
    @Schema(description = "Data type: STRING, INTEGER, LONG, BOOLEAN, DECIMAL, JSON", example = "BOOLEAN")
    private String dataType;

    @jakarta.validation.constraints.Size(max = 500, message = "Description must not exceed 500 characters")
    @Parameter(description = "Description of the setting")
    @Schema(description = "Description of the setting", example = "Enable or disable maintenance mode")
    private String description;

}
