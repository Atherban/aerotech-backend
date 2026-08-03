package com.aerotech.ced_ops_backend.report.dailystartup.repository;

import com.aerotech.ced_ops_backend.report.dailystartup.entity.DailyStartupEntry;
import com.aerotech.ced_ops_backend.report.dailystartup.entity.DailyStartupReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DailyStartupEntryRepository
        extends JpaRepository<DailyStartupEntry, Long> {

    @EntityGraph(attributePaths = {"parameter"})
    List<DailyStartupEntry> findByReport(DailyStartupReport report);

    @EntityGraph(attributePaths = {"report", "parameter"})
    List<DailyStartupEntry> findByReportIdIn(Collection<Long> reportIds);

    void deleteByReportId(Long reportId);

}
