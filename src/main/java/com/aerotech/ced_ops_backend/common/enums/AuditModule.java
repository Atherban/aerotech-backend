package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "System modules that are tracked in the audit log")
public enum AuditModule {
    AUTHENTICATION, // Authentication module
    USER_MANAGEMENT, // User management module
    SHIFT_MASTER, // Shift master data module
    LINE_MASTER, // Line master data module
    PROCESS_MASTER, // Process master data module
    PARAMETER_MASTER, // Parameter master data module
    PROCESS_MONITORING, // Process monitoring module
    CHEMICAL_CONSUMPTION, // Chemical consumption module
    DAILY_STARTUP, // Daily startup module
    FIRST_PIECE_INSPECTION, // First piece inspection module
    DAILY_INSPECTION, // Daily inspection module
    PRE_DELIVERY_INSPECTION, // Pre-delivery inspection module
    SYSTEM, // System-level actions
    DASHBOARD, // Dashboard module
    APPROVAL_CENTER // Approval center module
}
