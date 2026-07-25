package com.aerotech.ced_ops_backend.master.line.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.master.line.dto.CreateLineRequest;
import com.aerotech.ced_ops_backend.master.line.dto.LineResponse;
import com.aerotech.ced_ops_backend.master.line.dto.UpdateLineRequest;
import com.aerotech.ced_ops_backend.master.line.entity.Line;
import com.aerotech.ced_ops_backend.master.line.repository.LineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LineService {

    private final LineRepository lineRepository;

    public LineResponse create(CreateLineRequest request) {

        if (lineRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Line already exists.");
        }

        Line line = Line.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .active(true)
                .build();

        line = lineRepository.save(line);

        log.info("Line created: {}", line.getName());

        return toResponse(line);
    }

    @Transactional(readOnly = true)
    public List<LineResponse> getAll() {

        return lineRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LineResponse getById(Long id) {

        Line line = lineRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Line not found."));

        return toResponse(line);
    }

    public LineResponse update(Long id, UpdateLineRequest request) {

        Line line = lineRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Line not found."));

        if (!line.getName().equalsIgnoreCase(request.getName())
                && lineRepository.existsByNameIgnoreCase(request.getName().trim())) {

            throw new BadRequestException("Line already exists.");
        }

        line.setName(request.getName().trim());
        line.setDescription(request.getDescription());
        line.setDisplayOrder(request.getDisplayOrder());

        if (request.getActive() != null) {
            line.setActive(request.getActive());
        }

        line = lineRepository.save(line);

        log.info("Line updated: {}", line.getName());

        return toResponse(line);
    }

    public void delete(Long id) {

        Line line = lineRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Line not found."));

        line.setActive(false);

        lineRepository.save(line);

        log.info("Line deactivated: {}", line.getName());

    }

    private LineResponse toResponse(Line line) {

        return LineResponse.builder()
                .id(line.getId())
                .name(line.getName())
                .description(line.getDescription())
                .displayOrder(line.getDisplayOrder())
                .active(line.getActive())
                .build();

    }

}