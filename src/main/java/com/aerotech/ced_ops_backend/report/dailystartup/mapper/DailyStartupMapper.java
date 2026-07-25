package com.aerotech.ced_ops_backend.report.dailystartup.mapper;

import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.response.DailyStartupEntryResponse;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.response.DailyStartupResponse;
import com.aerotech.ced_ops_backend.report.dailystartup.entity.DailyStartupEntry;
import com.aerotech.ced_ops_backend.report.dailystartup.entity.DailyStartupReport;
import com.aerotech.ced_ops_backend.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyStartupMapper {

    public DailyStartupResponse toResponse(
            DailyStartupReport report,
            List<DailyStartupEntry> entries
    ) {

        if (report == null) {
            return null;
        }

        return DailyStartupResponse.builder()
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

    public List<DailyStartupResponse> toResponseList(
            List<DailyStartupReport> reports
    ) {

        if (reports == null) {
            return List.of();
        }

        return reports.stream()
                .map(report -> toResponse(report, List.of()))
                .toList();

    }

    public DailyStartupEntryResponse toEntryResponse(
            DailyStartupEntry entry
    ) {

        if (entry == null) {
            return null;
        }

        ParameterMaster parameter = entry.getParameter();

        return DailyStartupEntryResponse.builder()
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

    public List<DailyStartupEntryResponse> toEntryResponseList(
            List<DailyStartupEntry> entries
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
