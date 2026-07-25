package com.aerotech.ced_ops_backend.report.chemical.repository;

import com.aerotech.ced_ops_backend.report.chemical.entity.ChemicalConsumptionReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChemicalConsumptionReportRepository
        extends JpaRepository<ChemicalConsumptionReport, Long> {

    @EntityGraph(attributePaths = {"shift", "line", "createdBy", "approvedBy"})
    @Query("SELECT r FROM ChemicalConsumptionReport r ORDER BY r.id DESC")
    List<ChemicalConsumptionReport> findAllWithDetails();

    @EntityGraph(attributePaths = {"shift", "line", "createdBy", "approvedBy"})
    @Query("SELECT r FROM ChemicalConsumptionReport r WHERE r.id = :id")
    Optional<ChemicalConsumptionReport> findByIdWithDetails(Long id);

}
