package com.aerotech.ced_ops_backend.report.engine.repository;

import com.aerotech.ced_ops_backend.report.engine.entity.ReportSession;
import com.aerotech.ced_ops_backend.report.engine.enums.ReportSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportSessionRepository extends JpaRepository<ReportSession, Long> {

    List<ReportSession> findByCreatedByIdAndStatusOrderByCreatedAtDesc(Long userId, ReportSessionStatus status);

    List<ReportSession> findByStatus(ReportSessionStatus status);

    boolean existsByCurrentProcessId(Long processId);

}