package com.aerotech.ced_ops_backend.report.firstpieceinspection.repository;

import com.aerotech.ced_ops_backend.report.firstpieceinspection.entity.FirstPieceInspectionEntry;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.entity.FirstPieceInspectionReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FirstPieceInspectionEntryRepository
        extends JpaRepository<FirstPieceInspectionEntry, Long> {

    @EntityGraph(attributePaths = {"parameter"})
    List<FirstPieceInspectionEntry> findByReport(FirstPieceInspectionReport report);

    @EntityGraph(attributePaths = {"report", "parameter"})
    List<FirstPieceInspectionEntry> findByReportIdIn(Collection<Long> reportIds);

    void deleteByReportId(Long reportId);

}
