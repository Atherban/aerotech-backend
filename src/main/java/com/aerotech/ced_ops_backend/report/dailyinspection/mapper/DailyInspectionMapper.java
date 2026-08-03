package com.aerotech.ced_ops_backend.report.dailyinspection.mapper;

import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.response.DailyInspectionEntryResponse;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.response.DailyInspectionResponse;
import com.aerotech.ced_ops_backend.report.dailyinspection.entity.DailyInspectionEntry;
import com.aerotech.ced_ops_backend.report.dailyinspection.entity.DailyInspectionReport;
import com.aerotech.ced_ops_backend.report.support.BaseReportMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyInspectionMapper
        extends BaseReportMapper<DailyInspectionReport, DailyInspectionEntry, DailyInspectionResponse, DailyInspectionEntryResponse> {

    @Override
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

    @Override
    protected DailyInspectionEntryResponse toSingleEntryResponse(DailyInspectionEntry entry) {

        if (entry == null) {
            return null;
        }

        ParameterMaster parameter = entry.getParameter();

        return DailyInspectionEntryResponse.builder()
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