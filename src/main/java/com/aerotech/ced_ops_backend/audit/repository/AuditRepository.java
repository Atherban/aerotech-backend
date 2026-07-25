package com.aerotech.ced_ops_backend.audit.repository;

import com.aerotech.ced_ops_backend.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findTop10ByOrderByTimestampDesc();

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since")
    long countSince(@Param("since") LocalDateTime since);

    @Query("SELECT a.module, COUNT(a) FROM AuditLog a GROUP BY a.module ORDER BY COUNT(a) DESC")
    List<Object[]> countByModule();

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> countByAction();

}
