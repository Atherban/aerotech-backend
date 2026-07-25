package com.aerotech.ced_ops_backend.integration.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operational status of an integration connection")
public enum IntegrationStatus {
    ACTIVE, // Integration is enabled
    INACTIVE, // Integration is disabled
    CONNECTED, // Integration is connected
    DISCONNECTED, // Integration is disconnected
    ERROR, // Integration encountered an error
    TESTING // Integration is being tested
}
