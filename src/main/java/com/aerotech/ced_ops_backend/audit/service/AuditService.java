package com.aerotech.ced_ops_backend.audit.service;

import com.aerotech.ced_ops_backend.audit.dto.request.AuditFilterRequest;
import com.aerotech.ced_ops_backend.audit.dto.response.AuditLogResponse;
import com.aerotech.ced_ops_backend.audit.dto.response.AuditStatisticsResponse;
import com.aerotech.ced_ops_backend.audit.entity.AuditLog;
import com.aerotech.ced_ops_backend.audit.mapper.AuditMapper;
import com.aerotech.ced_ops_backend.audit.repository.AuditLogSpecification;
import com.aerotech.ced_ops_backend.audit.repository.AuditRepository;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    private final AuditRepository auditRepository;
    private final AuditMapper auditMapper;

    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "timestamp", "timestamp",
            "module", "module",
            "action", "action",
            "employeeId", "employeeId"
    );

    public PageResponse<AuditLogResponse> getLogs(AuditFilterRequest filter) {
        Sort sort = buildSort(filter.getSortBy(), filter.getSortDirection());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<AuditLog> auditLogPage = auditRepository.findAll(
                AuditLogSpecification.withFilters(filter), pageable
        );

        return PageResponse.from(auditLogPage.map(auditMapper::toResponse));
    }

    public List<AuditLogResponse> getRecentActivities() {
        List<AuditLog> recent = auditRepository.findTop10ByOrderByTimestampDesc();
        return auditMapper.toResponseList(recent);
    }

    public AuditStatisticsResponse getStatistics() {
        long totalLogs = auditRepository.count();
        long todayCount = auditRepository.countSince(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT));

        List<Object[]> moduleRows = auditRepository.countByModule();
        List<AuditStatisticsResponse.ModuleCount> logsByModule = new ArrayList<>();
        for (Object[] row : moduleRows) {
            logsByModule.add(AuditStatisticsResponse.ModuleCount.builder()
                    .module(((Enum<?>) row[0]).name())
                    .count(((Number) row[1]).longValue())
                    .build());
        }

        List<Object[]> actionRows = auditRepository.countByAction();
        List<AuditStatisticsResponse.ActionCount> logsByAction = new ArrayList<>();
        for (Object[] row : actionRows) {
            logsByAction.add(AuditStatisticsResponse.ActionCount.builder()
                    .action(((Enum<?>) row[0]).name())
                    .count(((Number) row[1]).longValue())
                    .build());
        }

        return AuditStatisticsResponse.builder()
                .totalLogs(totalLogs)
                .todayCount(todayCount)
                .logsByModule(logsByModule)
                .logsByAction(logsByAction)
                .build();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank() || !SORT_FIELD_MAP.containsKey(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "timestamp");
        }
        String field = SORT_FIELD_MAP.get(sortBy);
        Sort.Direction direction = Sort.Direction.DESC;
        if ("ASC".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.ASC;
        }
        return Sort.by(direction, field);
    }

}
