package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supported export formats for report output")
public enum ExportFormat {
    PDF, // Portable Document Format
    EXCEL, // Microsoft Excel format
    CSV // Comma-Separated Values format
}
