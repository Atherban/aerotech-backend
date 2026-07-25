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

    @Schema(description = "Setting ID", example = "1")
    private Long id;

    @Schema(description = "Unique setting key", example = "app.maintenance_mode")
    private String settingKey;

    @Schema(description = "Setting value", example = "false")
    private String settingValue;

    @Schema(description = "Setting category", example = "general")
    private String category;

    @Schema(description = "Data type", example = "BOOLEAN")
    private String dataType;

    @Schema(description = "Description of the setting", example = "Enable or disable maintenance mode")
    private String description;

    @Schema(description = "Whether the setting is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp", example = "2025-06-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-06-15T10:30:00")
    private LocalDateTime updatedAt;

}
