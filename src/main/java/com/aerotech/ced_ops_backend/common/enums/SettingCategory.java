package com.aerotech.ced_ops_backend.common.enums;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categories for grouping system settings")
public enum SettingCategory {
    GENERAL, // General system settings
    REPORT_SETTINGS, // Report-related settings
    NOTIFICATION_SETTINGS, // Notification-related settings
    ATTACHMENT_SETTINGS, // Attachment-related settings
    SECURITY_SETTINGS, // Security-related settings
    DASHBOARD_SETTINGS; // Dashboard-related settings

    public static final List<SettingCategory> VALUES = List.of(values());
}
