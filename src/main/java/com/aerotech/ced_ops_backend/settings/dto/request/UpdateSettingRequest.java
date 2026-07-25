package com.aerotech.ced_ops_backend.settings.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
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
    @Parameter(description = "New setting value")
    @Schema(description = "New setting value", example = "true")
    private String settingValue;

    @NotBlank(message = "Data type must not be blank")
    @Size(max = 20, message = "Data type must not exceed 20 characters")
    @Parameter(description = "Data type: STRING, INTEGER, LONG, BOOLEAN, DECIMAL, JSON")
    @Schema(description = "Data type: STRING, INTEGER, LONG, BOOLEAN, DECIMAL, JSON", example = "BOOLEAN")
    private String dataType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Parameter(description = "Description of the setting")
    @Schema(description = "Description of the setting", example = "Enable or disable maintenance mode")
    private String description;

    @Parameter(description = "Is active")
    @Schema(description = "Is active", example = "true")
    private Boolean isActive;

}
