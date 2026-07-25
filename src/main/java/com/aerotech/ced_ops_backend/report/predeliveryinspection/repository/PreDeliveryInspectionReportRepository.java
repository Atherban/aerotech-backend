package com.aerotech.ced_ops_backend.report.predeliveryinspection.repository;

import com.aerotech.ced_ops_backend.report.predeliveryinspection.entity.PreDeliveryInspectionReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PreDeliveryInspectionReportRepository
        extends JpaRepository<PreDeliveryInspectionReport, Long> {

    @EntityGraph(attributePaths = {"shift", "line", "createdBy", "approvedBy"})
    @Query("SELECT r FROM PreDeliveryInspectionReport r ORDER BY r.id DESC")
    List<PreDeliveryInspectionReport> findAllWithDetails();

    @EntityGraph(attributePaths = {"shift", "line", "createdBy", "approvedBy"})
    @Query("SELECT r FROM PreDeliveryInspectionReport r WHERE r.id = :id")
    Optional<PreDeliveryInspectionReport> findByIdWithDetails(Long id);

}
