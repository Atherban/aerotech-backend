package com.aerotech.ced_ops_backend.master.module.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.master.module.entity.Parameter;
import com.aerotech.ced_ops_backend.master.module.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Internal resolver for global {@link Parameter} references. Not exposed as an
 * API; used by the process/processParameter services to obtain entity instances.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParameterService {

    private final ParameterRepository parameterRepository;

    public Parameter getParameterOrThrow(Long id) {

        return parameterRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Parameter not found."));
    }

    public Parameter getActiveParameterOrThrow(Long id) {

        Parameter parameter = getParameterOrThrow(id);

        if (Boolean.FALSE.equals(parameter.getActive())) {
            throw new BadRequestException("Parameter is not active.");
        }

        return parameter;
    }

}