package com.aerotech.ced_ops_backend.settings.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Request to bulk update system settings")
public class BulkUpdateSettingsRequest {

    @Valid
    @NotEmpty(message = "Settings list must not be empty")
    @Parameter(description = "List of settings to update")
    @Schema(description = "List of settings to update")
    private List<BulkUpdateItem> settings;

    @Getter
    @Setter
    @Schema(description = "A single setting key-value pair for bulk update")
    public static class BulkUpdateItem {

        @NotBlank(message = "Setting key must not be blank")
        @Parameter(description = "Setting key")
        @Schema(description = "Setting key", example = "app.maintenance_mode")
        private String settingKey;

        @NotBlank(message = "Setting value must not be blank")
        @Parameter(description = "New value")
        @Schema(description = "New value", example = "true")
        private String settingValue;
    }

}
