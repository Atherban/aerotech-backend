package com.aerotech.ced_ops_backend.report.predeliveryinspection.repository;

import com.aerotech.ced_ops_backend.report.predeliveryinspection.entity.PreDeliveryInspectionEntry;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.entity.PreDeliveryInspectionReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PreDeliveryInspectionEntryRepository
        extends JpaRepository<PreDeliveryInspectionEntry, Long> {

    @EntityGraph(attributePaths = {"parameter"})
    List<PreDeliveryInspectionEntry> findByReport(PreDeliveryInspectionReport report);

    @EntityGraph(attributePaths = {"report", "parameter"})
    List<PreDeliveryInspectionEntry> findByReportIdIn(Collection<Long> reportIds);

    void deleteByReportId(Long reportId);

}
