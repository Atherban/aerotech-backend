package com.aerotech.ced_ops_backend.report.chemical.mapper;

import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.report.chemical.dto.response.ChemicalConsumptionEntryResponse;
import com.aerotech.ced_ops_backend.report.chemical.dto.response.ChemicalConsumptionResponse;
import com.aerotech.ced_ops_backend.report.chemical.entity.ChemicalConsumptionEntry;
import com.aerotech.ced_ops_backend.report.chemical.entity.ChemicalConsumptionReport;
import com.aerotech.ced_ops_backend.report.support.BaseReportMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChemicalConsumptionMapper
        extends BaseReportMapper<ChemicalConsumptionReport, ChemicalConsumptionEntry, ChemicalConsumptionResponse, ChemicalConsumptionEntryResponse> {

    @Override
    public ChemicalConsumptionResponse toResponse(
            ChemicalConsumptionReport report,
            List<ChemicalConsumptionEntry> entries
    ) {

        if (report == null) {
            return null;
        }

        return ChemicalConsumptionResponse.builder()
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
    protected ChemicalConsumptionEntryResponse toSingleEntryResponse(
            ChemicalConsumptionEntry entry
    ) {

        if (entry == null) {
            return null;
        }

        ParameterMaster parameter = entry.getParameter();

        return ChemicalConsumptionEntryResponse.builder()
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