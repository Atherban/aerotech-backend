package com.aerotech.ced_ops_backend.report.engine.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Completion status of a Recorded Process within a report session. Values: IN_PROGRESS - saved but not final; COMPLETED - the process was finished and saved")
public enum RecordedProcessStatus {
    IN_PROGRESS,
    COMPLETED
}