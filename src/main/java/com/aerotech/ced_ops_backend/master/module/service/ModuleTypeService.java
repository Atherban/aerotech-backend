package com.aerotech.ced_ops_backend.master.module.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.pagination.PageableResolver;
import com.aerotech.ced_ops_backend.common.pagination.SpecificationBuilder;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.master.module.dto.CreateModuleTypeRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ModuleTypeFilterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ModuleTypeResponse;
import com.aerotech.ced_ops_backend.master.module.dto.UpdateModuleTypeRequest;
import com.aerotech.ced_ops_backend.master.module.entity.ModuleType;
import com.aerotech.ced_ops_backend.master.module.repository.ModuleTypeRepository;
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
public class ModuleTypeService {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "id", "id",
            "name", "name",
            "active", "active",
            "createdAt", "createdAt"
    );

    private static final String DEFAULT_SORT = "name";

    private final ModuleTypeRepository moduleTypeRepository;

    public ModuleTypeResponse create(CreateModuleTypeRequest request) {

        if (moduleTypeRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Module type already exists.");
        }

        ModuleType moduleType = ModuleType.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .active(true)
                .build();

        moduleType = moduleTypeRepository.save(moduleType);

        log.info("Module type created: {}", moduleType.getName());

        return toResponse(moduleType);
    }

    @Transactional(readOnly = true)
    public List<ModuleTypeResponse> getAll() {

        return moduleTypeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ModuleTypeResponse> search(ModuleTypeFilterRequest filter) {

        Specification<ModuleType> spec = SpecificationBuilder.<ModuleType>builder()
                .keyword(filter.getKeyword(), "name", "description")
                .equals("active", filter.getActive())
                .build();

        Pageable pageable = PageableResolver.resolve(filter, SORT_COLUMNS, DEFAULT_SORT);

        Page<ModuleType> page = moduleTypeRepository.findAll(spec, pageable);

        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ModuleTypeResponse getById(Long id) {

        return toResponse(getModuleType(id));
    }

    public ModuleTypeResponse update(Long id, UpdateModuleTypeRequest request) {

        ModuleType moduleType = getModuleType(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            if (!moduleType.getName().equalsIgnoreCase(request.getName())
                    && moduleTypeRepository.existsByNameIgnoreCase(request.getName().trim())) {
                throw new BadRequestException("Module type already exists.");
            }
            moduleType.setName(request.getName().trim());
        }

        moduleType.setDescription(request.getDescription());

        moduleType = moduleTypeRepository.save(moduleType);

        log.info("Module type updated: {}", moduleType.getName());

        return toResponse(moduleType);
    }

    public void delete(Long id) {

        ModuleType moduleType = getModuleType(id);

        moduleType.setActive(false);

        moduleTypeRepository.save(moduleType);

        log.info("Module type deactivated: {}", moduleType.getName());
    }

    private ModuleType getModuleType(Long id) {

        return moduleTypeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Module type not found."));
    }

    private ModuleTypeResponse toResponse(ModuleType moduleType) {

        return ModuleTypeResponse.builder()
                .id(moduleType.getId())
                .name(moduleType.getName())
                .description(moduleType.getDescription())
                .active(moduleType.getActive())
                .build();
    }

}