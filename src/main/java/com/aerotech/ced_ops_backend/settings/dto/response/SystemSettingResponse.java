package com.aerotech.ced_ops_backend.settings.dto.response;

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
@Schema(description = "Response containing system setting details")
public class SystemSettingResponse {

    @Schema(description = "Setting ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Unique setting key", example = "app.maintenance_mode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String settingKey;

    @Schema(description = "Setting value", example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String settingValue;

    @Schema(description = "Setting category", example = "GENERAL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String category;

    @Schema(description = "Data type", example = "BOOLEAN", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String dataType;

    @Schema(description = "Description of the setting", example = "Enable or disable maintenance mode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "Whether the setting is active", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isActive;

    @Schema(description = "Creation timestamp", example = "2025-06-15T10:30:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-06-15T10:30:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime updatedAt;

}
