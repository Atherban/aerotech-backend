package com.aerotech.ced_ops_backend.master.parameter.service;

import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.pagination.PageableResolver;
import com.aerotech.ced_ops_backend.common.pagination.SpecificationBuilder;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.master.parameter.dto.CreateParameterRequest;
import com.aerotech.ced_ops_backend.master.parameter.dto.ParameterFilterRequest;
import com.aerotech.ced_ops_backend.master.parameter.dto.ParameterResponse;
import com.aerotech.ced_ops_backend.master.parameter.dto.UpdateParameterRequest;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.master.parameter.repository.ParameterMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ParameterMasterService {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "id", "id",
            "parameterName", "parameterName",
            "displayOrder", "displayOrder",
            "inputType", "inputType",
            "active", "active",
            "visible", "visible",
            "createdAt", "createdAt"
    );

    private static final String DEFAULT_SORT = "displayOrder";

    private final ParameterMasterRepository parameterRepository;

    public ParameterResponse create(CreateParameterRequest request) {

        if (parameterRepository.existsByReportTypeAndParameterNameIgnoreCase(
                request.getReportType(),
                request.getParameterName().trim())) {

            throw new BadRequestException("Parameter already exists for this report type.");
        }

        ParameterMaster parameter = ParameterMaster.builder()
                .reportType(request.getReportType())
                .parameterName(request.getParameterName().trim())
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .unit(request.getUnit())
                .inputType(request.getInputType())
                .displayOrder(request.getDisplayOrder())
                .active(true)
                .testMethod(request.getTestMethod())
                .frequency(request.getFrequency())
                .mandatory(request.getMandatory())
                .visible(request.getVisible())
                .defaultValue(request.getDefaultValue())
                .build();

        parameter = parameterRepository.save(parameter);

        log.info("Parameter created: {} for report type {}", parameter.getParameterName(), parameter.getReportType());

        return toResponse(parameter);
    }

    @Transactional(readOnly = true)
    public List<ParameterResponse> getAll() {

        return parameterRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ParameterResponse> search(ParameterFilterRequest filter) {

        Specification<ParameterMaster> spec = SpecificationBuilder.<ParameterMaster>builder()
                .keyword(filter.getKeyword(),
                        "parameterName", "unit", "testMethod")
                .equals("reportType", filter.getReportType())
                .equals("inputType", filter.getInputType())
                .equals("active", filter.getActive())
                .equals("visible", filter.getVisible())
                .build();

        Pageable pageable = PageableResolver.resolve(filter, SORT_COLUMNS, DEFAULT_SORT);

        Page<ParameterMaster> page = parameterRepository.findAll(spec, pageable);

        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<ParameterResponse> getByReportType(ReportType reportType) {

        return parameterRepository.findByReportTypeOrderByDisplayOrderAsc(reportType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParameterResponse getById(Long id) {

        return toResponse(getParameter(id));
    }

    public ParameterResponse update(Long id, UpdateParameterRequest request) {

        ParameterMaster parameter = getParameter(id);

        parameter.setParameterName(request.getParameterName().trim());
        parameter.setMinValue(request.getMinValue());
        parameter.setMaxValue(request.getMaxValue());
        parameter.setUnit(request.getUnit());
        parameter.setInputType(request.getInputType());
        parameter.setDisplayOrder(request.getDisplayOrder());
        parameter.setTestMethod(request.getTestMethod());
        parameter.setFrequency(request.getFrequency());

        if (request.getMandatory() != null) {
            parameter.setMandatory(request.getMandatory());
        }

        if (request.getVisible() != null) {
            parameter.setVisible(request.getVisible());
        }

        parameter.setDefaultValue(request.getDefaultValue());

        parameter = parameterRepository.save(parameter);

        log.info("Parameter updated: {}", parameter.getParameterName());

        return toResponse(parameter);
    }

    public void delete(Long id) {

        ParameterMaster parameter = getParameter(id);

        parameter.setActive(false);

        parameterRepository.save(parameter);

        log.info("Parameter deactivated: {}", parameter.getParameterName());
    }

    private ParameterMaster getParameter(Long id) {

        return parameterRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Parameter not found."));
    }

    private ParameterResponse toResponse(ParameterMaster parameter) {

        return ParameterResponse.builder()
                .id(parameter.getId())
                .reportType(parameter.getReportType())
                .parameterName(parameter.getParameterName())
                .minValue(parameter.getMinValue())
                .maxValue(parameter.getMaxValue())
                .unit(parameter.getUnit())
                .inputType(parameter.getInputType())
                .displayOrder(parameter.getDisplayOrder())
                .active(parameter.getActive())
                .testMethod(parameter.getTestMethod())
                .frequency(parameter.getFrequency())
                .mandatory(parameter.getMandatory())
                .visible(parameter.getVisible())
                .defaultValue(parameter.getDefaultValue())
                .build();
    }

}
