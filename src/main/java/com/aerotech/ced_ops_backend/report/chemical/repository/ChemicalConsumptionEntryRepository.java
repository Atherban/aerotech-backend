package com.aerotech.ced_ops_backend.report.chemical.repository;

import com.aerotech.ced_ops_backend.report.chemical.entity.ChemicalConsumptionEntry;
import com.aerotech.ced_ops_backend.report.chemical.entity.ChemicalConsumptionReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ChemicalConsumptionEntryRepository
        extends JpaRepository<ChemicalConsumptionEntry, Long> {

    @EntityGraph(attributePaths = {"parameter"})
    List<ChemicalConsumptionEntry> findByReport(ChemicalConsumptionReport report);

    @EntityGraph(attributePaths = {"report", "parameter"})
    List<ChemicalConsumptionEntry> findByReportIdIn(Collection<Long> reportIds);

    void deleteByReportId(Long reportId);

}
