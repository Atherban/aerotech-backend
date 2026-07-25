package com.aerotech.ced_ops_backend.report.dailyinspection.repository;

import com.aerotech.ced_ops_backend.report.dailyinspection.entity.DailyInspectionReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DailyInspectionReportRepository
        extends JpaRepository<DailyInspectionReport, Long> {

    @EntityGraph(attributePaths = {"shift", "line", "process", "createdBy", "approvedBy"})
    @Query("SELECT r FROM DailyInspectionReport r ORDER BY r.id DESC")
    List<DailyInspectionReport> findAllWithDetails();

    @EntityGraph(attributePaths = {"shift", "line", "process", "createdBy", "approvedBy"})
    @Query("SELECT r FROM DailyInspectionReport r WHERE r.id = :id")
    Optional<DailyInspectionReport> findByIdWithDetails(Long id);

}
