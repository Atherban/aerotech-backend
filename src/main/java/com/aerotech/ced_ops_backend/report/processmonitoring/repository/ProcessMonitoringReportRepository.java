package com.aerotech.ced_ops_backend.report.processmonitoring.repository;

import com.aerotech.ced_ops_backend.report.processmonitoring.entity.ProcessMonitoringReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProcessMonitoringReportRepository
        extends JpaRepository<ProcessMonitoringReport, Long> {

    @EntityGraph(attributePaths = {"shift", "line", "createdBy", "approvedBy"})
    @Query("SELECT r FROM ProcessMonitoringReport r ORDER BY r.id DESC")
    List<ProcessMonitoringReport> findAllWithDetails();

    @EntityGraph(attributePaths = {"shift", "line", "createdBy", "approvedBy"})
    @Query("SELECT r FROM ProcessMonitoringReport r WHERE r.id = :id")
    Optional<ProcessMonitoringReport> findByIdWithDetails(Long id);

}
