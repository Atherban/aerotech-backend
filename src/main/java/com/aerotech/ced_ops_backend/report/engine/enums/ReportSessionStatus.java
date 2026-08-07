package com.aerotech.ced_ops_backend.report.engine.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of a Report Session (work in progress). Values: IN_PROGRESS - session is being filled; COMPLETED - every process was saved and the report was submitted")
public enum ReportSessionStatus {
    IN_PROGRESS,
    COMPLETED
}