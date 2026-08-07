package com.aerotech.ced_ops_backend.master.module.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.pagination.PageableResolver;
import com.aerotech.ced_ops_backend.common.pagination.SpecificationBuilder;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.master.module.dto.CreateProcessRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ProcessFilterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ProcessResponse;
import com.aerotech.ced_ops_backend.master.module.dto.UpdateProcessRequest;
import com.aerotech.ced_ops_backend.master.module.entity.Process;
import com.aerotech.ced_ops_backend.master.module.entity.TemplateVersion;
import com.aerotech.ced_ops_backend.master.module.enums.ProcessStatus;
import com.aerotech.ced_ops_backend.master.module.enums.TemplateVersionStatus;
import com.aerotech.ced_ops_backend.master.module.repository.ProcessRepository;
import com.aerotech.ced_ops_backend.master.module.repository.TemplateVersionRepository;
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
public class ProcessService {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "id", "id",
            "name", "name",
            "displayOrder", "displayOrder",
            "status", "status",
            "createdAt", "createdAt"
    );

    private static final String DEFAULT_SORT = "displayOrder";

    private final ProcessRepository processRepository;
    private final TemplateVersionRepository templateVersionRepository;

    public ProcessResponse create(CreateProcessRequest request) {

        TemplateVersion version = getEditableVersion(request.getTemplateVersionId());

        if (processRepository.existsByTemplateVersionIdAndNameIgnoreCase(version.getId(), request.getName().trim())) {
            throw new BadRequestException("Process already exists in this template version.");
        }

        Process process = Process.builder()
                .templateVersion(version)
                .name(request.getName().trim())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .status(ProcessStatus.ACTIVE)
                .build();

        process = processRepository.save(process);

        log.info("Process created: {} in version {}", process.getName(), version.getVersionNumber());

        return toResponse(process);
    }

    @Transactional(readOnly = true)
    public List<ProcessResponse> getAll() {

        return processRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProcessResponse> search(ProcessFilterRequest filter) {

        Specification<Process> spec = SpecificationBuilder.<Process>builder()
                .keyword(filter.getKeyword(), "name", "description")
                .equals("templateVersion.id", filter.getTemplateVersionId())
                .equals("status", filter.getStatus())
                .build();

        Pageable pageable = PageableResolver.resolve(filter, SORT_COLUMNS, DEFAULT_SORT);

        Page<Process> page = processRepository.findAll(spec, pageable);

        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ProcessResponse getById(Long id) {

        return toResponse(getProcess(id));
    }

    public ProcessResponse update(Long id, UpdateProcessRequest request) {

        Process process = getProcess(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            if (processRepository.existsByTemplateVersionIdAndNameIgnoreCase(
                    process.getTemplateVersion().getId(), request.getName().trim())) {
                throw new BadRequestException("Process already exists in this template version.");
            }
            process.setName(request.getName().trim());
        }

        process.setDescription(request.getDescription());
        process.setDisplayOrder(request.getDisplayOrder());

        process = processRepository.save(process);

        log.info("Process updated: {}", process.getName());

        return toResponse(process);
    }

    public void archive(Long id) {

        Process process = getProcess(id);

        process.setStatus(ProcessStatus.ARCHIVED);
        processRepository.save(process);

        log.info("Process archived: {}", process.getName());
    }

    /** Resolves a template version that can still accept content edits (DRAFT, or ACTIVE for additive Phase-2 master work). */
    private TemplateVersion getEditableVersion(Long templateVersionId) {

        TemplateVersion version = templateVersionRepository.findById(templateVersionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Template version not found."));

        if (version.getStatus() == TemplateVersionStatus.SUPERSEDED) {
            throw new BadRequestException("Cannot edit a superseded template version.");
        }

        return version;
    }

    private Process getProcess(Long id) {

        return processRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Process not found."));
    }

    private ProcessResponse toResponse(Process process) {

        return ProcessResponse.builder()
                .id(process.getId())
                .templateVersionId(process.getTemplateVersion().getId())
                .name(process.getName())
                .description(process.getDescription())
                .displayOrder(process.getDisplayOrder())
                .status(process.getStatus())
                .build();
    }

}