package com.aerotech.ced_ops_backend.report.processmonitoring.repository;

import com.aerotech.ced_ops_backend.report.processmonitoring.entity.ProcessMonitoringEntry;
import com.aerotech.ced_ops_backend.report.processmonitoring.entity.ProcessMonitoringReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProcessMonitoringEntryRepository
        extends JpaRepository<ProcessMonitoringEntry, Long> {

    @EntityGraph(attributePaths = {"parameter"})
    List<ProcessMonitoringEntry> findByReport(ProcessMonitoringReport report);

    @EntityGraph(attributePaths = {"report", "parameter"})
    List<ProcessMonitoringEntry> findByReportIdIn(Collection<Long> reportIds);

    void deleteByReportId(Long reportId);

}
