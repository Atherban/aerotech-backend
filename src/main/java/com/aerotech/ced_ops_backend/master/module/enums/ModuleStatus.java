package com.aerotech.ced_ops_backend.master.module.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of a Module. Values: DRAFT - Module being built, not usable; ACTIVE - Module usable for new reports; ARCHIVED - Module not usable for new reports but retained for historical reports")
public enum ModuleStatus {
    DRAFT,
    ACTIVE,
    ARCHIVED
}