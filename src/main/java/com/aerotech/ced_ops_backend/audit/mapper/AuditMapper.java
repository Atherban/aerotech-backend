package com.aerotech.ced_ops_backend.audit.mapper;

import com.aerotech.ced_ops_backend.audit.dto.response.AuditLogResponse;
import com.aerotech.ced_ops_backend.audit.entity.AuditLog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuditMapper {

    public AuditLogResponse toResponse(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .timestamp(auditLog.getTimestamp())
                .userId(auditLog.getUserId())
                .employeeId(auditLog.getEmployeeId())
                .username(auditLog.getUsername())
                .userRole(auditLog.getUserRole())
                .module(auditLog.getModule() != null ? auditLog.getModule().name() : null)
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .action(auditLog.getAction() != null ? auditLog.getAction().name() : null)
                .previousValue(auditLog.getPreviousValue())
                .newValue(auditLog.getNewValue())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .metadata(auditLog.getMetadata())
                .build();
    }

    public List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs) {
        return auditLogs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
