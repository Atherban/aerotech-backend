package com.aerotech.ced_ops_backend.master.process.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.master.process.dto.CreateProcessRequest;
import com.aerotech.ced_ops_backend.master.process.dto.ProcessResponse;
import com.aerotech.ced_ops_backend.master.process.dto.UpdateProcessRequest;
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
public class ProcessMasterService {

    private final ProcessMasterRepository processRepository;

    public ProcessResponse create(CreateProcessRequest request) {

        if (processRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Process already exists.");
        }

        ProcessMaster process = ProcessMaster.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .active(true)
                .build();

        process = processRepository.save(process);

        log.info("Process created: {}", process.getName());

        return toResponse(process);
    }

    @Transactional(readOnly = true)
    public List<ProcessResponse> getAll() {

        return processRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcessResponse getById(Long id) {

        ProcessMaster process = processRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Process not found."));

        return toResponse(process);
    }

    public ProcessResponse update(Long id, UpdateProcessRequest request) {

        ProcessMaster process = processRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Process not found."));

        if (!process.getName().equalsIgnoreCase(request.getName())
                && processRepository.existsByNameIgnoreCase(request.getName().trim())) {

            throw new BadRequestException("Process already exists.");
        }

        process.setName(request.getName().trim());
        process.setDescription(request.getDescription());
        process.setDisplayOrder(request.getDisplayOrder());

        if (request.getActive() != null) {
            process.setActive(request.getActive());
        }

        process = processRepository.save(process);

        log.info("Process updated: {}", process.getName());

        return toResponse(process);
    }

    public void delete(Long id) {

        ProcessMaster process = processRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Process not found."));

        // Soft Delete
        process.setActive(false);

        processRepository.save(process);

        log.info("Process deactivated: {}", process.getName());
    }

    private ProcessResponse toResponse(ProcessMaster process) {

        return ProcessResponse.builder()
                .id(process.getId())
                .name(process.getName())
                .description(process.getDescription())
                .displayOrder(process.getDisplayOrder())
                .active(process.getActive())
                .build();
    }

}