package com.aerotech.ced_ops_backend.master.module.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.pagination.PageableResolver;
import com.aerotech.ced_ops_backend.common.pagination.SpecificationBuilder;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.master.module.dto.CreateParameterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ParameterFilterRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ParameterResponse;
import com.aerotech.ced_ops_backend.master.module.dto.UpdateParameterRequest;
import com.aerotech.ced_ops_backend.master.module.entity.Parameter;
import com.aerotech.ced_ops_backend.master.module.repository.ParameterRepository;
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
public class ParameterCrudService {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "id", "id",
            "name", "name",
            "inputType", "inputType",
            "active", "active",
            "createdAt", "createdAt"
    );

    private static final String DEFAULT_SORT = "name";

    private final ParameterRepository parameterRepository;

    public ParameterResponse create(CreateParameterRequest request) {

        if (parameterRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Parameter already exists.");
        }

        Parameter parameter = Parameter.builder()
                .name(request.getName().trim())
                .inputType(request.getInputType())
                .description(request.getDescription())
                .active(true)
                .build();

        parameter = parameterRepository.save(parameter);

        log.info("Global parameter created: {}", parameter.getName());

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

        Specification<Parameter> spec = SpecificationBuilder.<Parameter>builder()
                .keyword(filter.getKeyword(), "name", "description")
                .equals("inputType", filter.getInputType())
                .equals("active", filter.getActive())
                .build();

        Pageable pageable = PageableResolver.resolve(filter, SORT_COLUMNS, DEFAULT_SORT);

        Page<Parameter> page = parameterRepository.findAll(spec, pageable);

        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ParameterResponse getById(Long id) {

        return toResponse(getParameter(id));
    }

    public ParameterResponse update(Long id, UpdateParameterRequest request) {

        Parameter parameter = getParameter(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            if (!parameter.getName().equalsIgnoreCase(request.getName())
                    && parameterRepository.existsByNameIgnoreCase(request.getName().trim())) {
                throw new BadRequestException("Parameter already exists.");
            }
            parameter.setName(request.getName().trim());
        }

        if (request.getInputType() != null) {
            parameter.setInputType(request.getInputType());
        }

        parameter.setDescription(request.getDescription());

        parameter = parameterRepository.save(parameter);

        log.info("Global parameter updated: {}", parameter.getName());

        return toResponse(parameter);
    }

    public void delete(Long id) {

        Parameter parameter = getParameter(id);

        parameter.setActive(false);
        parameterRepository.save(parameter);

        log.info("Global parameter deactivated: {}", parameter.getName());
    }

    private Parameter getParameter(Long id) {

        return parameterRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Parameter not found."));
    }

    private ParameterResponse toResponse(Parameter parameter) {

        return ParameterResponse.builder()
                .id(parameter.getId())
                .name(parameter.getName())
                .inputType(parameter.getInputType())
                .description(parameter.getDescription())
                .active(parameter.getActive())
                .build();
    }

}