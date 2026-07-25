package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data types supported for system settings values")
public enum SettingDataType {
    STRING, // String data type
    INTEGER, // Integer data type
    LONG, // Long data type
    BOOLEAN, // Boolean data type
    DECIMAL, // Decimal data type
    JSON // JSON data type
}
