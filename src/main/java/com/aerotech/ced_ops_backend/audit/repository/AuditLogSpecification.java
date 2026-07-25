package com.aerotech.ced_ops_backend.audit.repository;

import com.aerotech.ced_ops_backend.audit.dto.request.AuditFilterRequest;
import com.aerotech.ced_ops_backend.audit.entity.AuditLog;
import com.aerotech.ced_ops_backend.common.enums.AuditAction;
import com.aerotech.ced_ops_backend.common.enums.AuditModule;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {

    public static Specification<AuditLog> withFilters(AuditFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), filter.getUserId()));
            }
            if (filter.getModule() != null && !filter.getModule().isBlank()) {
                try {
                    AuditModule module = AuditModule.valueOf(filter.getModule().toUpperCase());
                    predicates.add(cb.equal(root.get("module"), module));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (filter.getAction() != null && !filter.getAction().isBlank()) {
                try {
                    AuditAction action = AuditAction.valueOf(filter.getAction().toUpperCase());
                    predicates.add(cb.equal(root.get("action"), action));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (filter.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), filter.getDateFrom()));
            }
            if (filter.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), filter.getDateTo()));
            }

            query.orderBy(cb.desc(root.get("timestamp")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
