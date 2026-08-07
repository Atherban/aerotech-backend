package com.aerotech.ced_ops_backend.report.engine.repository;

import com.aerotech.ced_ops_backend.report.engine.entity.CompletedReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompletedReportRepository extends JpaRepository<CompletedReport, Long> {

    boolean existsByReportNumber(String reportNumber);

    Optional<CompletedReport> findBySessionId(Long sessionId);

    List<CompletedReport> findByCreatedByIdOrderBySubmittedAtDesc(Long userId);

    long countByModuleId(Long moduleId);

    boolean existsByModuleId(Long moduleId);

}