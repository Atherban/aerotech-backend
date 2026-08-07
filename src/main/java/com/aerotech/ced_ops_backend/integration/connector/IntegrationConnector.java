package com.aerotech.ced_ops_backend.integration.connector;

import com.aerotech.ced_ops_backend.integration.entity.Integration;
import com.aerotech.ced_ops_backend.integration.enums.IntegrationStatus;
import com.aerotech.ced_ops_backend.integration.enums.IntegrationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationConnector {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public IntegrationType supportedType() {
        return IntegrationType.WEBHOOK;
    }

    public IntegrationStatus testConnection(Integration integration) {
        try {
            Map<String, Object> config = parseConfig(integration.getConfigJson());
            String url = (String) config.getOrDefault("url", "");

            if (url.isBlank()) {
                log.warn("Webhook URL is empty for integration: {}", integration.getName());
                return IntegrationStatus.ERROR;
            }

            HttpHeaders headers = buildHeaders(config);
            Map<String, Object> testPayload = Map.of(
                    "type", "test",
                    "timestamp", LocalDateTime.now().toString(),
                    "source", "ced-ops-backend"
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(testPayload, headers);

            LocalDateTime start = LocalDateTime.now();
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, String.class);
            long duration = Duration.between(start, LocalDateTime.now()).toMillis();

            log.info("Webhook test for '{}' completed in {}ms with status: {}",
                    integration.getName(), duration, response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                return IntegrationStatus.CONNECTED;
            } else {
                return IntegrationStatus.ERROR;
            }
        } catch (ResourceAccessException e) {
            log.error("Webhook connection timeout for '{}': {}", integration.getName(), e.getMessage());
            return IntegrationStatus.DISCONNECTED;
        } catch (Exception e) {
            log.error("Webhook test failed for '{}': {}", integration.getName(), e.getMessage());
            return IntegrationStatus.ERROR;
        }
    }

    public boolean isValidConfig(String configJson) {
        try {
            Map<String, Object> config = parseConfig(configJson);
            String url = (String) config.get("url");
            return url != null && !url.isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> parseConfig(String configJson) {
        try {
            return objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    private HttpHeaders buildHeaders(Map<String, Object> config) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (config.containsKey("headers") && config.get("headers") instanceof Map<?, ?> customHeaders) {
            customHeaders.forEach((key, value) -> {
                if (key instanceof String && value instanceof String) {
                    headers.set((String) key, (String) value);
                }
            });
        }

        return headers;
    }
}
