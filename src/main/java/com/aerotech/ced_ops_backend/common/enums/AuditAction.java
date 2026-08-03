package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Actions that are tracked in the audit log. Values: LOGIN - User login; LOGOUT - User logout; FAILED_LOGIN - Failed login attempt; PASSWORD_CHANGE - Password change; TOKEN_REFRESH - Authentication token refresh; CREATE - Resource creation; UPDATE - Resource update; DELETE - Resource deletion; ACTIVATE - Resource activation; DEACTIVATE - Resource deactivation; ROLE_CHANGE - User role change; DRAFT_SAVED - Draft saved; SUBMIT - Resource submitted; APPROVE - Resource approved; REJECT - Resource rejected; CANCEL - Resource cancelled; ATTACHMENT_UPLOAD - Attachment uploaded; ATTACHMENT_DELETE - Attachment deleted")
public enum AuditAction {
    LOGIN, // User login
    LOGOUT, // User logout
    FAILED_LOGIN, // Failed login attempt
    PASSWORD_CHANGE, // Password change
    TOKEN_REFRESH, // Authentication token refresh
    CREATE, // Resource creation
    UPDATE, // Resource update
    DELETE, // Resource deletion
    ACTIVATE, // Resource activation
    DEACTIVATE, // Resource deactivation
    ROLE_CHANGE, // User role change
    DRAFT_SAVED, // Draft saved
    SUBMIT, // Resource submitted
    APPROVE, // Resource approved
    REJECT, // Resource rejected
    CANCEL, // Resource cancelled
    ATTACHMENT_UPLOAD, // Attachment uploaded
    ATTACHMENT_DELETE // Attachment deleted
}
