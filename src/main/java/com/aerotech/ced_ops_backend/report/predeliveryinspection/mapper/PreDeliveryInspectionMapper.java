package com.aerotech.ced_ops_backend.report.predeliveryinspection.mapper;

import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.response.PreDeliveryInspectionEntryResponse;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.response.PreDeliveryInspectionResponse;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.entity.PreDeliveryInspectionEntry;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.entity.PreDeliveryInspectionReport;
import com.aerotech.ced_ops_backend.report.support.BaseReportMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PreDeliveryInspectionMapper
        extends BaseReportMapper<PreDeliveryInspectionReport, PreDeliveryInspectionEntry, PreDeliveryInspectionResponse, PreDeliveryInspectionEntryResponse> {

    @Override
    public PreDeliveryInspectionResponse toResponse(
            PreDeliveryInspectionReport report,
            List<PreDeliveryInspectionEntry> entries
    ) {

        if (report == null) {
            return null;
        }

        return PreDeliveryInspectionResponse.builder()
                .id(report.getId())
                .reportNumber(report.getReportNumber())
                .reportDate(report.getReportDate())
                .shift(report.getShift() != null ? report.getShift().getName() : null)
                .line(report.getLine() != null ? report.getLine().getName() : null)
                .productPartNumber(report.getProductPartNumber())
                .batchNumber(report.getBatchNumber())
                .inspectorName(report.getInspectorName())
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
    protected PreDeliveryInspectionEntryResponse toSingleEntryResponse(PreDeliveryInspectionEntry entry) {

        if (entry == null) {
            return null;
        }

        ParameterMaster parameter = entry.getParameter();

        return PreDeliveryInspectionEntryResponse.builder()
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