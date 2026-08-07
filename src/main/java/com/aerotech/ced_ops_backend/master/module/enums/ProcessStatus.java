package com.aerotech.ced_ops_backend.master.module.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of a Process. Values: DRAFT - being configured, not yet usable; ACTIVE - usable in reports; ARCHIVED - not usable for new reports but retained for historical reports")
public enum ProcessStatus {
    DRAFT,
    ACTIVE,
    ARCHIVED
}