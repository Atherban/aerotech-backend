package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Types of notifications that can be sent to users")
public enum NotificationType {
    WELCOME, // Welcome notification for new users
    PASSWORD_CHANGED, // Password changed successfully
    REPORT_CREATED, // Report has been created
    REPORT_SUBMITTED, // Report has been submitted
    REPORT_APPROVED, // Report has been approved
    REPORT_REJECTED, // Report has been rejected
    REPORT_RETURNED, // Report has been returned for revisions
    PENDING_APPROVAL, // Report is pending approval
    APPROVAL_REMINDER, // Reminder for pending approval
    USER_CREATED, // New user created
    USER_ACTIVATED, // User account activated
    USER_DEACTIVATED, // User account deactivated
    ROLE_CHANGED, // User role changed
    EXPORT_COMPLETED, // Export operation completed
    ATTACHMENT_UPLOADED, // Attachment uploaded to a report
    MAINTENANCE_NOTICE // System maintenance notice
}
