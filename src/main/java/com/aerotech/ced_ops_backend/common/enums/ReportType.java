package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of report that can be created in the system")
public enum ReportType {

    PROCESS_MONITORING, // Monitors process parameters during production
    PDI, // Pre-delivery inspection report
    DAILY_STARTUP, // Daily startup checklist report
    CHEMICAL_CONSUMPTION, // Tracks chemical consumption
    FIRST_PIECE_INSPECTION, // First piece inspection report
    DAILY_INSPECTION // Daily inspection report

}