package com.aerotech.ced_ops_backend.master.shift.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.master.shift.dto.CreateShiftRequest;
import com.aerotech.ced_ops_backend.master.shift.dto.ShiftResponse;
import com.aerotech.ced_ops_backend.master.shift.dto.UpdateShiftRequest;
import com.aerotech.ced_ops_backend.master.shift.entity.Shift;
import com.aerotech.ced_ops_backend.master.shift.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShiftService {

    private final ShiftRepository shiftRepository;

    public ShiftResponse create(CreateShiftRequest request) {

        if (shiftRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Shift already exists.");
        }

        Shift shift = Shift.builder()
                .name(request.getName().trim())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .active(true)
                .build();

        shift = shiftRepository.save(shift);

        log.info("Shift created: {}", shift.getName());

        return toResponse(shift);
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> getAll() {

        return shiftRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShiftResponse getById(Long id) {

        return toResponse(getShiftEntity(id));
    }

    public ShiftResponse update(Long id, UpdateShiftRequest request) {

        Shift shift = getShiftEntity(id);

        if (!shift.getName().equalsIgnoreCase(request.getName())
                && shiftRepository.existsByNameIgnoreCase(request.getName())) {

            throw new BadRequestException("Shift already exists.");
        }

        shift.setName(request.getName().trim());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());

        if (request.getActive() != null) {
            shift.setActive(request.getActive());
        }

        shift = shiftRepository.save(shift);

        log.info("Shift updated: {}", shift.getName());

        return toResponse(shift);
    }

    public void delete(Long id) {

        Shift shift = getShiftEntity(id);

        shift.setActive(false);
        shiftRepository.save(shift);

        log.info("Shift deleted: {}", shift.getName());
    }

    /**
     * Resolves the shift that covers the given time, handling overnight shifts
     * (startTime >= endTime wraps past midnight).
     */
    @Transactional(readOnly = true)
    public Shift findShiftFor(LocalTime time) {

        LocalTime currentTime = time != null ? time : LocalTime.now();

        List<Shift> activeShifts = shiftRepository.findAllByActiveTrueOrderByStartTimeAsc();

        if (activeShifts.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No active shift is configured. Please contact the administrator.");
        }

        return activeShifts.stream()
                .filter(shift -> shift.covers(currentTime))
                .findFirst()
                .orElseGet(() -> activeShifts.get(0));
    }

    @Transactional(readOnly = true)
    public ShiftResponse getCurrentShift() {

        return toResponse(findShiftFor(LocalTime.now()));
    }

    @Transactional(readOnly = true)
    public Shift getShiftEntity(Long id) {

        return shiftRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shift not found."));
    }

    private ShiftResponse toResponse(Shift shift) {

        return ShiftResponse.builder()
                .id(shift.getId())
                .name(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .active(shift.getActive())
                .build();
    }

}
