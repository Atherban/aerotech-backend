package com.aerotech.ced_ops_backend.report.firstpieceinspection.repository;

import com.aerotech.ced_ops_backend.report.firstpieceinspection.entity.FirstPieceInspectionReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FirstPieceInspectionReportRepository
        extends JpaRepository<FirstPieceInspectionReport, Long> {

    @EntityGraph(attributePaths = {"shift", "line", "process", "createdBy", "approvedBy"})
    @Query("SELECT r FROM FirstPieceInspectionReport r ORDER BY r.id DESC")
    List<FirstPieceInspectionReport> findAllWithDetails();

    @EntityGraph(attributePaths = {"shift", "line", "process", "createdBy", "approvedBy"})
    @Query("SELECT r FROM FirstPieceInspectionReport r WHERE r.id = :id")
    Optional<FirstPieceInspectionReport> findByIdWithDetails(Long id);

}
