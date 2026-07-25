package com.aerotech.ced_ops_backend.master.parameter.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.master.parameter.dto.CreateParameterRequest;
import com.aerotech.ced_ops_backend.master.parameter.dto.ParameterResponse;
import com.aerotech.ced_ops_backend.master.parameter.dto.UpdateParameterRequest;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.master.parameter.repository.ParameterMasterRepository;
import com.aerotech.ced_ops_backend.master.process.entity.ProcessMaster;
import com.aerotech.ced_ops_backend.master.process.repository.ProcessMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ParameterMasterService {

    private final ParameterMasterRepository parameterRepository;
    private final ProcessMasterRepository processRepository;

    public ParameterResponse create(CreateParameterRequest request) {

        ProcessMaster process = processRepository.findById(request.getProcessId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Process not found."));

        if (parameterRepository.existsByProcessIdAndParameterNameIgnoreCase(
                request.getProcessId(),
                request.getParameterName().trim())) {

            throw new BadRequestException("Parameter already exists.");
        }

        ParameterMaster parameter = ParameterMaster.builder()
                .process(process)
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
                .build();

        parameter = parameterRepository.save(parameter);

        log.info("Parameter created: {}", parameter.getParameterName());

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
    public List<ParameterResponse> getByProcess(Long processId) {

        return parameterRepository.findByProcessIdOrderByDisplayOrderAsc(processId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParameterResponse getById(Long id) {

        ParameterMaster parameter = parameterRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Parameter not found."));

        return toResponse(parameter);
    }

    public ParameterResponse update(Long id, UpdateParameterRequest request) {

        ParameterMaster parameter = parameterRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Parameter not found."));

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

        parameter = parameterRepository.save(parameter);

        log.info("Parameter updated: {}", parameter.getParameterName());

        return toResponse(parameter);
    }

    public void delete(Long id) {

        ParameterMaster parameter = parameterRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Parameter not found."));

        parameter.setActive(false);

        parameterRepository.save(parameter);

        log.info("Parameter deactivated: {}", parameter.getParameterName());

    }

    private ParameterResponse toResponse(ParameterMaster parameter) {

        return ParameterResponse.builder()
                .id(parameter.getId())
                .processId(parameter.getProcess().getId())
                .processName(parameter.getProcess().getName())
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
                .build();
    }

}