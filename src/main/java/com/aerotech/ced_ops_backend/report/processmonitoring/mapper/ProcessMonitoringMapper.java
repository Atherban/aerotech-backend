package com.aerotech.ced_ops_backend.report.processmonitoring.mapper;

import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.response.ProcessMonitoringEntryResponse;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.response.ProcessMonitoringResponse;
import com.aerotech.ced_ops_backend.report.processmonitoring.entity.ProcessMonitoringEntry;
import com.aerotech.ced_ops_backend.report.processmonitoring.entity.ProcessMonitoringReport;
import com.aerotech.ced_ops_backend.report.support.BaseReportMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessMonitoringMapper
        extends BaseReportMapper<ProcessMonitoringReport, ProcessMonitoringEntry, ProcessMonitoringResponse, ProcessMonitoringEntryResponse> {

    @Override
    public ProcessMonitoringResponse toResponse(
            ProcessMonitoringReport report,
            List<ProcessMonitoringEntry> entries
    ) {

        if (report == null) {
            return null;
        }

        return ProcessMonitoringResponse.builder()
                .id(report.getId())
                .reportNumber(report.getReportNumber())
                .reportDate(report.getReportDate())
                .shift(report.getShift() != null ? report.getShift().getName() : null)
                .line(report.getLine() != null ? report.getLine().getName() : null)
                .createdBy(fullName(report.getCreatedBy()))
                .approvedBy(fullName(report.getApprovedBy()))
                .status(report.getStatus())
                .remarks(report.getRemarks())
                .approvedAt(report.getApprovedAt())
                .createdAt(report.getCreatedAt())
                .entries(toEntryResponseList(entries))
                .build();

    }

    @Override
    protected ProcessMonitoringEntryResponse toSingleEntryResponse(ProcessMonitoringEntry entry) {

        if (entry == null) {
            return null;
        }

        ParameterMaster parameter = entry.getParameter();

        return ProcessMonitoringEntryResponse.builder()
                .id(entry.getId())
                .parameterId(parameter != null ? parameter.getId() : null)
                .parameterName(parameter != null ? parameter.getParameterName() : null)
                .minValue(parameter != null ? parameter.getMinValue() : null)
                .maxValue(parameter != null ? parameter.getMaxValue() : null)
                .observedValue(entry.getObservedValue())
                .unit(parameter != null ? parameter.getUnit() : null)
                .inspectionResult(entry.getInspectionResult())
                .remark(entry.getRemark())
                .build();

    }

}