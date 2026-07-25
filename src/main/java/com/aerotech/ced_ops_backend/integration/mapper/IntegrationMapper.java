package com.aerotech.ced_ops_backend.integration.mapper;

import com.aerotech.ced_ops_backend.integration.dto.response.IntegrationExecutionHistoryResponse;
import com.aerotech.ced_ops_backend.integration.dto.response.IntegrationResponse;
import com.aerotech.ced_ops_backend.integration.entity.Integration;
import com.aerotech.ced_ops_backend.integration.entity.IntegrationExecutionHistory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class IntegrationMapper {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "secret", "apiKey", "api_key", "api-key",
            "token", "authToken", "auth_token", "accessKey", "access_key",
            "privateKey", "private_key", "clientSecret", "client_secret",
            "secretKey", "secret_key", "appSecret", "app_secret"
    );

    private final ObjectMapper objectMapper;

    public IntegrationResponse toResponse(Integration entity) {
        if (entity == null) return null;
        return IntegrationResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .status(entity.getStatus())
                .configJson(maskSensitiveConfig(entity.getConfigJson()))
                .retryCount(entity.getRetryCount())
                .timeoutSeconds(entity.getTimeoutSeconds())
                .isActive(entity.getIsActive())
                .lastTestedAt(entity.getLastTestedAt())
                .lastTestStatus(entity.getLastTestStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public IntegrationExecutionHistoryResponse toHistoryResponse(IntegrationExecutionHistory entity) {
        if (entity == null) return null;
        return IntegrationExecutionHistoryResponse.builder()
                .id(entity.getId())
                .integrationId(entity.getIntegrationId())
                .integrationName(entity.getIntegrationName())
                .integrationType(entity.getIntegrationType())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .durationMs(entity.getDurationMs())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .retryCount(entity.getRetryCount())
                .responseCode(entity.getResponseCode())
                .triggerType(entity.getTriggerType())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String maskSensitiveConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) return configJson;
        try {
            Map<String, Object> config = objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
            boolean masked = false;
            for (String key : config.keySet()) {
                if (SENSITIVE_KEYS.contains(key) && config.get(key) instanceof String) {
                    config.put(key, "****");
                    masked = true;
                }
            }
            if (!masked) return configJson;
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            return configJson;
        }
    }
}
