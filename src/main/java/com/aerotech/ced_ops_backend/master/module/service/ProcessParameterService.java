package com.aerotech.ced_ops_backend.master.module.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.master.module.dto.CreateProcessParameterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ParameterSummaryResponse;
import com.aerotech.ced_ops_backend.master.module.dto.ProcessParameterResponse;
import com.aerotech.ced_ops_backend.master.module.dto.UpdateProcessParameterRequest;
import com.aerotech.ced_ops_backend.master.module.entity.Parameter;
import com.aerotech.ced_ops_backend.master.module.entity.Process;
import com.aerotech.ced_ops_backend.master.module.entity.ProcessParameter;
import com.aerotech.ced_ops_backend.master.module.repository.ProcessParameterRepository;
import com.aerotech.ced_ops_backend.master.module.repository.ProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProcessParameterService {

    private final ProcessParameterRepository processParameterRepository;
    private final ProcessRepository processRepository;
    private final ParameterService parameterService;

    public ProcessParameterResponse create(Long processId, CreateProcessParameterRequest request) {

        Process process = processRepository.findById(processId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Process not found."));

        Parameter parameter = parameterService.getActiveParameterOrThrow(request.getParameterId());

        if (processParameterRepository.existsByProcessIdAndParameterId(processId, parameter.getId())) {
            throw new BadRequestException("Parameter is already bound to this process.");
        }

        ProcessParameter binding = ProcessParameter.builder()
                .process(process)
                .parameter(parameter)
                .displayOrder(request.getDisplayOrder())
                .mandatory(request.getMandatory() == null || request.getMandatory())
                .visible(request.getVisible() == null || request.getVisible())
                .defaultValue(request.getDefaultValue())
                .unit(request.getUnit())
                .minimumValue(request.getMinimumValue())
                .maximumValue(request.getMaximumValue())
                .active(true)
                .build();

        binding = processParameterRepository.save(binding);

        log.info("Parameter binding created for process {}", process.getName());

        return toResponse(binding);
    }

    @Transactional(readOnly = true)
    public List<ProcessParameterResponse> getByProcess(Long processId) {

        processRepository.findById(processId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Process not found."));

        return processParameterRepository.findByProcessIdOrderByDisplayOrderAsc(processId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcessParameterResponse getById(Long id) {

        return toResponse(getBinding(id));
    }

    public ProcessParameterResponse update(Long id, UpdateProcessParameterRequest request) {

        ProcessParameter binding = getBinding(id);

        binding.setDisplayOrder(request.getDisplayOrder());

        if (request.getMandatory() != null) {
            binding.setMandatory(request.getMandatory());
        }

        if (request.getVisible() != null) {
            binding.setVisible(request.getVisible());
        }

        binding.setDefaultValue(request.getDefaultValue());
        binding.setUnit(request.getUnit());
        binding.setMinimumValue(request.getMinimumValue());
        binding.setMaximumValue(request.getMaximumValue());

        binding = processParameterRepository.save(binding);

        log.info("Process parameter binding updated: {}", binding.getId());

        return toResponse(binding);
    }

    public void delete(Long id) {

        ProcessParameter binding = getBinding(id);

        binding.setActive(false);
        processParameterRepository.save(binding);

        log.info("Process parameter binding deactivated: {}", binding.getId());
    }

    private ProcessParameter getBinding(Long id) {

        return processParameterRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Process parameter binding not found."));
    }

    private ProcessParameterResponse toResponse(ProcessParameter binding) {

        return ProcessParameterResponse.builder()
                .id(binding.getId())
                .processId(binding.getProcess().getId())
                .parameter(ParameterSummaryResponse.builder()
                        .id(binding.getParameter().getId())
                        .name(binding.getParameter().getName())
                        .build())
                .displayOrder(binding.getDisplayOrder())
                .mandatory(binding.getMandatory())
                .visible(binding.getVisible())
                .defaultValue(binding.getDefaultValue())
                .unit(binding.getUnit())
                .minimumValue(binding.getMinimumValue())
                .maximumValue(binding.getMaximumValue())
                .active(binding.getActive())
                .inputType(binding.getParameter().getInputType())
                .build();
    }

}