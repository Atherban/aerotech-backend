package com.aerotech.ced_ops_backend.integration.service;

import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.integration.connector.IntegrationConnector;
import com.aerotech.ced_ops_backend.integration.dto.request.CreateIntegrationRequest;
import com.aerotech.ced_ops_backend.integration.dto.request.UpdateIntegrationRequest;
import com.aerotech.ced_ops_backend.integration.dto.response.IntegrationExecutionHistoryResponse;
import com.aerotech.ced_ops_backend.integration.dto.response.IntegrationResponse;
import com.aerotech.ced_ops_backend.integration.entity.Integration;
import com.aerotech.ced_ops_backend.integration.entity.IntegrationExecutionHistory;
import com.aerotech.ced_ops_backend.integration.enums.IntegrationStatus;
import com.aerotech.ced_ops_backend.integration.enums.IntegrationType;
import com.aerotech.ced_ops_backend.integration.mapper.IntegrationMapper;
import com.aerotech.ced_ops_backend.integration.repository.IntegrationExecutionHistoryRepository;
import com.aerotech.ced_ops_backend.integration.repository.IntegrationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private final IntegrationExecutionHistoryRepository historyRepository;
    private final IntegrationMapper mapper;
    private final List<IntegrationConnector> connectors;

    private final Map<IntegrationType, IntegrationConnector> connectorMap = new HashMap<>();

    @PostConstruct
    public void initConnectors() {
        for (IntegrationConnector connector : connectors) {
            connectorMap.put(connector.supportedType(), connector);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<IntegrationResponse> findAll(IntegrationType type, String search,
                                                     int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return PageResponse.from(integrationRepository.findByFilters(type, search, pageable)
                .map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public IntegrationResponse findById(Long id) {
        return mapper.toResponse(findIntegration(id));
    }

    @Transactional
    public IntegrationResponse create(CreateIntegrationRequest request, String createdBy) {
        if (integrationRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Integration with name '" + request.getName() + "' already exists");
        }

        Integration integration = Integration.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .status(IntegrationStatus.INACTIVE)
                .configJson(request.getConfigJson())
                .retryCount(request.getRetryCount() != null ? request.getRetryCount() : 3)
                .timeoutSeconds(request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : 30)
                .isActive(true)
                .createdBy(createdBy)
                .build();

        integration = integrationRepository.save(integration);
        log.info("Integration created: {} (type={})", integration.getName(), integration.getType());
        return mapper.toResponse(integration);
    }

    @Transactional
    public IntegrationResponse update(Long id, UpdateIntegrationRequest request) {
        Integration integration = findIntegration(id);

        if (request.getName() != null) {
            integration.setName(request.getName());
        }
        if (request.getDescription() != null) {
            integration.setDescription(request.getDescription());
        }
        if (request.getConfigJson() != null) {
            integration.setConfigJson(request.getConfigJson());
        }
        if (request.getRetryCount() != null) {
            integration.setRetryCount(request.getRetryCount());
        }
        if (request.getTimeoutSeconds() != null) {
            integration.setTimeoutSeconds(request.getTimeoutSeconds());
        }

        integration = integrationRepository.save(integration);
        log.info("Integration updated: {} (id={})", integration.getName(), integration.getId());
        return mapper.toResponse(integration);
    }

    @Transactional
    public void delete(Long id) {
        Integration integration = findIntegration(id);
        historyRepository.deleteByIntegrationId(id);
        integrationRepository.delete(integration);
        log.info("Integration deleted: {} (id={})", integration.getName(), id);
    }

    @Transactional
    public IntegrationResponse testConnection(Long id) {
        Integration integration = findIntegration(id);
        IntegrationConnector connector = findConnector(integration.getType());

        integration.setStatus(IntegrationStatus.TESTING);
        integration = integrationRepository.save(integration);

        LocalDateTime startTime = LocalDateTime.now();
        IntegrationStatus resultStatus;
        String errorMessage = null;
        String responseCode = null;

        try {
            resultStatus = connector.testConnection(integration);
        } catch (Exception e) {
            resultStatus = IntegrationStatus.ERROR;
            errorMessage = e.getMessage();
        }

        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = Duration.between(startTime, endTime).toMillis();

        integration.setStatus(resultStatus);
        integration.setLastTestedAt(endTime);
        integration.setLastTestStatus(resultStatus.name());
        integration = integrationRepository.save(integration);

        IntegrationExecutionHistory history = IntegrationExecutionHistory.builder()
                .integrationId(integration.getId())
                .integrationName(integration.getName())
                .integrationType(integration.getType())
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .status(resultStatus)
                .errorMessage(errorMessage)
                .retryCount(0)
                .responseCode(responseCode)
                .triggerType("MANUAL")
                .build();

        historyRepository.save(history);

        if (resultStatus == IntegrationStatus.CONNECTED) {
            log.info("Connection test passed for integration: {} (id={})", integration.getName(), id);
        } else {
            log.warn("Connection test failed for integration: {} (id={}) - status={}, error={}",
                    integration.getName(), id, resultStatus, errorMessage);
        }

        return mapper.toResponse(integration);
    }

    @Transactional
    public IntegrationResponse enable(Long id) {
        Integration integration = findIntegration(id);
        integration.setIsActive(true);
        if (integration.getStatus() == IntegrationStatus.INACTIVE) {
            integration.setStatus(IntegrationStatus.ACTIVE);
        }
        integration = integrationRepository.save(integration);
        log.info("Integration enabled: {} (id={})", integration.getName(), id);
        return mapper.toResponse(integration);
    }

    @Transactional
    public IntegrationResponse disable(Long id) {
        Integration integration = findIntegration(id);
        integration.setIsActive(false);
        integration.setStatus(IntegrationStatus.INACTIVE);
        integration = integrationRepository.save(integration);
        log.info("Integration disabled: {} (id={})", integration.getName(), id);
        return mapper.toResponse(integration);
    }

    @Transactional(readOnly = true)
    public PageResponse<IntegrationExecutionHistoryResponse> getHistory(Long integrationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<IntegrationExecutionHistory> historyPage;
        if (integrationId != null) {
            historyPage = historyRepository.findByIntegrationIdOrderByCreatedAtDesc(integrationId, pageable);
        } else {
            historyPage = historyRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return PageResponse.from(historyPage.map(mapper::toHistoryResponse));
    }

    private Integration findIntegration(Long id) {
        return integrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Integration not found with id: " + id));
    }

    private IntegrationConnector findConnector(IntegrationType type) {
        IntegrationConnector connector = connectorMap.get(type);
        if (connector == null) {
            throw new UnsupportedOperationException(
                    "No connector found for integration type: " + type);
        }
        return connector;
    }
}
