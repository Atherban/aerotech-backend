package com.aerotech.ced_ops_backend.notification.dto.response;

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
@Schema(description = "Response containing notification details")
public class NotificationResponse {

    @Schema(description = "Notification ID", example = "1")
    private Long id;

    @Schema(description = "Notification title", example = "Report Approved")
    private String title;

    @Schema(description = "Notification message body", example = "Your report REP-001234 has been approved.")
    private String message;

    @Schema(description = "Notification type", example = "APPROVAL")
    private String type;

    @Schema(description = "Related module name", example = "quality")
    private String relatedModule;

    @Schema(description = "Related entity ID", example = "REP-001234")
    private String relatedEntityId;

    @Schema(description = "Notification priority", example = "HIGH")
    private String priority;

    @Schema(description = "Whether the notification has been read", example = "false")
    private Boolean isRead;

    @Schema(description = "Timestamp when the notification was read", example = "2025-06-15T12:00:00")
    private LocalDateTime readAt;

    @Schema(description = "Creation timestamp", example = "2025-06-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Additional metadata (JSON)")
    private String metadata;

}
