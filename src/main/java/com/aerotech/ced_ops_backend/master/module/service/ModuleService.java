package com.aerotech.ced_ops_backend.master.module.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.pagination.PageableResolver;
import com.aerotech.ced_ops_backend.common.pagination.SpecificationBuilder;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.master.module.dto.CreateModuleRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ModuleFilterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ModuleResponse;
import com.aerotech.ced_ops_backend.master.module.dto.ModuleTypeSummaryResponse;
import com.aerotech.ced_ops_backend.master.module.dto.TemplateVersionResponse;
import com.aerotech.ced_ops_backend.master.module.dto.UpdateModuleRequest;
import com.aerotech.ced_ops_backend.master.module.entity.Module;
import com.aerotech.ced_ops_backend.master.module.entity.ModuleType;
import com.aerotech.ced_ops_backend.master.module.entity.TemplateVersion;
import com.aerotech.ced_ops_backend.master.module.enums.ModuleStatus;
import com.aerotech.ced_ops_backend.master.module.enums.TemplateVersionStatus;
import com.aerotech.ced_ops_backend.master.module.repository.ModuleRepository;
import com.aerotech.ced_ops_backend.master.module.repository.ModuleTypeRepository;
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
public class ModuleService {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "id", "id",
            "name", "name",
            "prefix", "prefix",
            "status", "status",
            "createdAt", "createdAt"
    );

    private static final String DEFAULT_SORT = "name";

    private final ModuleRepository moduleRepository;
    private final ModuleTypeRepository moduleTypeRepository;
    private final TemplateVersionRepository templateVersionRepository;

    public ModuleResponse create(CreateModuleRequest request) {

        if (moduleRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Module already exists.");
        }

        if (moduleRepository.existsByPrefixIgnoreCase(request.getPrefix().trim())) {
            throw new BadRequestException("Module prefix already in use.");
        }

        ModuleType moduleType = moduleTypeRepository.findById(request.getModuleTypeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Module type not found."));

        if (Boolean.FALSE.equals(moduleType.getActive())) {
            throw new BadRequestException("Module type is not active.");
        }

        Module module = Module.builder()
                .moduleType(moduleType)
                .name(request.getName().trim())
                .prefix(request.getPrefix().trim().toUpperCase())
                .description(request.getDescription())
                .status(ModuleStatus.DRAFT)
                .build();

        module = moduleRepository.save(module);

        TemplateVersion version = TemplateVersion.builder()
                .module(module)
                .versionNumber(1)
                .status(TemplateVersionStatus.DRAFT)
                .changeNote(request.getChangeNote())
                .build();

        templateVersionRepository.save(version);

        log.info("Module created with initial template version: {} [{}]", module.getName(), module.getPrefix());

        return toResponse(module);
    }

    @Transactional(readOnly = true)
    public List<ModuleResponse> getAll() {

        return moduleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ModuleResponse> search(ModuleFilterRequest filter) {

        Specification<Module> spec = SpecificationBuilder.<Module>builder()
                .keyword(filter.getKeyword(), "name", "prefix", "description")
                .equals("moduleType.id", filter.getModuleTypeId())
                .equals("status", filter.getStatus())
                .build();

        Pageable pageable = PageableResolver.resolve(filter, SORT_COLUMNS, DEFAULT_SORT);

        Page<Module> page = moduleRepository.findAll(spec, pageable);

        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ModuleResponse getById(Long id) {

        return toResponse(getModule(id));
    }

    public ModuleResponse update(Long id, UpdateModuleRequest request) {

        Module module = getModule(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            if (!module.getName().equalsIgnoreCase(request.getName())
                    && moduleRepository.existsByNameIgnoreCase(request.getName().trim())) {
                throw new BadRequestException("Module already exists.");
            }
            module.setName(request.getName().trim());
        }

        if (request.getPrefix() != null && !request.getPrefix().isBlank()) {
            String prefix = request.getPrefix().trim().toUpperCase();
            if (!module.getPrefix().equalsIgnoreCase(prefix)
                    && moduleRepository.existsByPrefixIgnoreCase(prefix)) {
                throw new BadRequestException("Module prefix already in use.");
            }
            module.setPrefix(prefix);
        }

        module.setDescription(request.getDescription());

        module = moduleRepository.save(module);

        log.info("Module updated: {}", module.getName());

        return toResponse(module);
    }

    public void archive(Long id) {

        Module module = getModule(id);

        if (module.getStatus() == ModuleStatus.ACTIVE) {
            module.setStatus(ModuleStatus.ARCHIVED);
            moduleRepository.save(module);
            log.info("Module archived: {}", module.getName());
        }
    }

    private Module getModule(Long id) {

        return moduleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Module not found."));
    }

    private ModuleResponse toResponse(Module module) {

        TemplateVersion latestActive = templateVersionRepository
                .findTopByModuleIdAndStatusOrderByVersionNumberDesc(module.getId(), TemplateVersionStatus.ACTIVE)
                .orElse(null);

        ModuleTypeSummaryResponse moduleType = module.getModuleType() != null
                ? ModuleTypeSummaryResponse.builder()
                        .id(module.getModuleType().getId())
                        .name(module.getModuleType().getName())
                        .build()
                : null;

        TemplateVersionResponse version = latestActive != null
                ? TemplateVersionResponse.builder()
                        .id(latestActive.getId())
                        .moduleId(module.getId())
                        .versionNumber(latestActive.getVersionNumber())
                        .status(latestActive.getStatus())
                        .changeNote(latestActive.getChangeNote())
                        .build()
                : null;

        return ModuleResponse.builder()
                .id(module.getId())
                .moduleType(moduleType)
                .name(module.getName())
                .prefix(module.getPrefix())
                .description(module.getDescription())
                .status(module.getStatus())
                .latestActiveVersion(version)
                .build();
    }

}