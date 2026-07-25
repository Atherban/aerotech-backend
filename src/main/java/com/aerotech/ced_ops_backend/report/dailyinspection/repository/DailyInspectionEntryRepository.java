package com.aerotech.ced_ops_backend.report.dailyinspection.repository;

import com.aerotech.ced_ops_backend.report.dailyinspection.entity.DailyInspectionEntry;
import com.aerotech.ced_ops_backend.report.dailyinspection.entity.DailyInspectionReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DailyInspectionEntryRepository
        extends JpaRepository<DailyInspectionEntry, Long> {

    @EntityGraph(attributePaths = {"parameter", "parameter.process"})
    List<DailyInspectionEntry> findByReport(DailyInspectionReport report);

    @EntityGraph(attributePaths = {"report", "parameter", "parameter.process"})
    List<DailyInspectionEntry> findByReportIdIn(Collection<Long> reportIds);

    void deleteByReportId(Long reportId);

}
