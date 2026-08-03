package com.aerotech.ced_ops_backend.integration.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of external system integration. Values: ERP - Enterprise Resource Planning integration; SAP - SAP integration; MES - Manufacturing Execution System integration; PLC_SCADA - PLC/SCADA integration; IOT_DEVICE - IoT device integration; EMAIL - Email integration; SMS - SMS integration; TEAMS - Microsoft Teams integration; SLACK - Slack integration; WEBHOOK - Webhook integration; REST_API - REST API integration; GRAPHQL_API - GraphQL API integration; FTP_SFTP - FTP/SFTP integration; CLOUD_STORAGE - Cloud storage integration; OTHER - Other integration types")
public enum IntegrationType {
    ERP, // Enterprise Resource Planning integration
    SAP, // SAP integration
    MES, // Manufacturing Execution System integration
    PLC_SCADA, // PLC/SCADA integration
    IOT_DEVICE, // IoT device integration
    EMAIL, // Email integration
    SMS, // SMS integration
    TEAMS, // Microsoft Teams integration
    SLACK, // Slack integration
    WEBHOOK, // Webhook integration
    REST_API, // REST API integration
    GRAPHQL_API, // GraphQL API integration
    FTP_SFTP, // FTP/SFTP integration
    CLOUD_STORAGE, // Cloud storage integration
    OTHER // Other integration types
}
