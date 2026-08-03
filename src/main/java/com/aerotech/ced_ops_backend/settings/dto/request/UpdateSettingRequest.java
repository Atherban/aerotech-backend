package com.aerotech.ced_ops_backend.settings.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to update an existing system setting")
public class UpdateSettingRequest {

    @NotBlank(message = "Setting value must not be blank")
    @Schema(description = "New setting value", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private String settingValue;

    @NotBlank(message = "Data type must not be blank")
    @Size(max = 20, message = "Data type must not exceed 20 characters")
    @Schema(description = "Data type", example = "BOOLEAN", allowableValues = {"STRING", "INTEGER", "LONG", "BOOLEAN", "DECIMAL", "JSON"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String dataType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Description of the setting", example = "Enable or disable maintenance mode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "Is active", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isActive;

}
