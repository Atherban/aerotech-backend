package com.aerotech.ced_ops_backend.report.dailyinspection.mapper;

import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.response.DailyInspectionEntryResponse;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.response.DailyInspectionResponse;
import com.aerotech.ced_ops_backend.report.dailyinspection.entity.DailyInspectionEntry;
import com.aerotech.ced_ops_backend.report.dailyinspection.entity.DailyInspectionReport;
import com.aerotech.ced_ops_backend.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyInspectionMapper {

    public DailyInspectionResponse toResponse(
            DailyInspectionReport report,
            List<DailyInspectionEntry> entries
    ) {

        if (report == null) {
            return null;
        }

        return DailyInspectionResponse.builder()
                .id(report.getId())
                .reportNumber(report.getReportNumber())
                .reportDate(report.getReportDate())
                .shift(report.getShift() != null ? report.getShift().getName() : null)
                .line(report.getLine() != null ? report.getLine().getName() : null)
                .process(report.getProcess() != null ? report.getProcess().getName() : null)
                .inspectorName(report.getInspectorName())
                .correctiveAction(report.getCorrectiveAction())
                .createdBy(fullName(report.getCreatedBy()))
                .approvedBy(fullName(report.getApprovedBy()))
                .status(report.getStatus())
                .remarks(report.getRemarks())
                .approvedAt(report.getApprovedAt())
                .createdAt(report.getCreatedAt())
                .entries(toEntryResponseList(entries))
                .build();

    }

    public List<DailyInspectionResponse> toResponseList(
            List<DailyInspectionReport> reports
    ) {

        if (reports == null) {
            return List.of();
        }

        return reports.stream()
                .map(report -> toResponse(report, List.of()))
                .toList();

    }

    public DailyInspectionEntryResponse toEntryResponse(
            DailyInspectionEntry entry
    ) {

        if (entry == null) {
            return null;
        }

        ParameterMaster parameter = entry.getParameter();

        return DailyInspectionEntryResponse.builder()
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

    public List<DailyInspectionEntryResponse> toEntryResponseList(
            List<DailyInspectionEntry> entries
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
