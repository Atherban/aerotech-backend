package com.aerotech.ced_ops_backend.report.processmonitoring.mapper;

import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.response.ProcessMonitoringEntryResponse;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.response.ProcessMonitoringResponse;
import com.aerotech.ced_ops_backend.report.processmonitoring.entity.ProcessMonitoringEntry;
import com.aerotech.ced_ops_backend.report.processmonitoring.entity.ProcessMonitoringReport;
import com.aerotech.ced_ops_backend.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessMonitoringMapper {

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

    public List<ProcessMonitoringResponse> toResponseList(
            List<ProcessMonitoringReport> reports
    ) {

        if (reports == null) {
            return List.of();
        }

        return reports.stream()
                .map(report -> toResponse(report, List.of()))
                .toList();

    }

    public ProcessMonitoringEntryResponse toEntryResponse(
            ProcessMonitoringEntry entry
    ) {

        if (entry == null) {
            return null;
        }

        ParameterMaster parameter = entry.getParameter();

        return ProcessMonitoringEntryResponse.builder()
                .id(entry.getId())
                .parameterId(parameter != null ? parameter.getId() : null)
                .processName(parameter != null && parameter.getProcess() != null
                        ? parameter.getProcess().getName()
                        : null)
                .parameterName(parameter != null ? parameter.getParameterName() : null)
                .minValue(parameter != null ? parameter.getMinValue() : null)
                .maxValue(parameter != null ? parameter.getMaxValue() : null)
                .observedValue(entry.getObservedValue())
                .unit(parameter != null ? parameter.getUnit() : null)
                .inspectionResult(entry.getInspectionResult())
                .remark(entry.getRemark())
                .build();

    }

    public List<ProcessMonitoringEntryResponse> toEntryResponseList(
            List<ProcessMonitoringEntry> entries
    ) {

        if (entries == null) {
            return List.of();
        }

        return entries.stream()
                .map(this::toEntryResponse)
                .toList();

    }

    private String fullName(User user) {

        if (user == null) {
            return null;
        }

        return (user.getFirstName() + " " + user.getLastName()).trim();

    }

}
