package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Priority level of a notification. Values: LOW - Low priority notification; MEDIUM - Medium priority notification; HIGH - High priority notification; CRITICAL - Critical priority notification")
public enum NotificationPriority {
    LOW, // Low priority notification
    MEDIUM, // Medium priority notification
    HIGH, // High priority notification
    CRITICAL // Critical priority notification
}
