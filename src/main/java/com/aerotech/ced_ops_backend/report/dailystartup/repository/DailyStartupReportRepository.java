package com.aerotech.ced_ops_backend.report.dailystartup.repository;

import com.aerotech.ced_ops_backend.report.dailystartup.entity.DailyStartupReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DailyStartupReportRepository
        extends JpaRepository<DailyStartupReport, Long> {

    @EntityGraph(attributePaths = {"shift", "line", "createdBy", "approvedBy"})
    @Query("SELECT r FROM DailyStartupReport r ORDER BY r.id DESC")
    List<DailyStartupReport> findAllWithDetails();

    @EntityGraph(attributePaths = {"shift", "line", "createdBy", "approvedBy"})
    @Query("SELECT r FROM DailyStartupReport r WHERE r.id = :id")
    Optional<DailyStartupReport> findByIdWithDetails(Long id);

}
