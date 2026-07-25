package com.aerotech.ced_ops_backend.report.chemical.service;

import com.aerotech.ced_ops_backend.common.enums.ReportStatus;
import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.service.ValidationService;
import com.aerotech.ced_ops_backend.common.util.ReportNumberGenerator;
import com.aerotech.ced_ops_backend.master.line.entity.Line;
import com.aerotech.ced_ops_backend.master.line.repository.LineRepository;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.master.parameter.repository.ParameterMasterRepository;
import com.aerotech.ced_ops_backend.master.shift.entity.Shift;
import com.aerotech.ced_ops_backend.master.shift.repository.ShiftRepository;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.ApproveChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.ChemicalConsumptionEntryRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.CreateChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.SubmitChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.response.ChemicalConsumptionResponse;
import com.aerotech.ced_ops_backend.report.chemical.entity.ChemicalConsumptionEntry;
import com.aerotech.ced_ops_backend.report.chemical.entity.ChemicalConsumptionReport;
import com.aerotech.ced_ops_backend.report.chemical.mapper.ChemicalConsumptionMapper;
import com.aerotech.ced_ops_backend.report.chemical.repository.ChemicalConsumptionEntryRepository;
import com.aerotech.ced_ops_backend.report.chemical.repository.ChemicalConsumptionReportRepository;
import com.aerotech.ced_ops_backend.user.entity.User;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChemicalConsumptionService {

    private final ChemicalConsumptionReportRepository reportRepository;

    private final ChemicalConsumptionEntryRepository entryRepository;

    private final ValidationService validationService;

    private final ShiftRepository shiftRepository;

    private final LineRepository lineRepository;

    private final ParameterMasterRepository parameterRepository;

    private final UserRepository userRepository;

    private final ReportNumberGenerator reportNumberGenerator;

    private final ChemicalConsumptionMapper mapper;

    private User currentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmployeeId(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));

    }

    private Shift getShift(Long id) {

        return shiftRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shift not found."));

    }

    private Line getLine(Long id) {

        return lineRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Line not found."));

    }

    private ParameterMaster getParameter(Long id) {

        return parameterRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Parameter not found."));

    }

    private String nextReportNumber() {

        long sequence = reportRepository.count() + 1;

        return reportNumberGenerator.generate(
                ReportType.CHEMICAL_CONSUMPTION,
                sequence
        );

    }

    public ChemicalConsumptionResponse create(CreateChemicalConsumptionRequest request) {

        ChemicalConsumptionReport report = new ChemicalConsumptionReport();

        report.setReportNumber(nextReportNumber());
        report.setReportType(ReportType.CHEMICAL_CONSUMPTION);
        report.setReportDate(request.getReportDate());
        report.setShift(getShift(request.getShiftId()));
        report.setLine(getLine(request.getLineId()));
        report.setStatus(ReportStatus.DRAFT);
        report.setCreatedBy(currentUser());
        report.setRemarks(request.getRemarks());

        report = reportRepository.save(report);

        ChemicalConsumptionReport savedReport = report;

        List<ChemicalConsumptionEntry> entries = request.getEntries()
                .stream()
                .map(entryRequest -> buildEntry(savedReport, entryRequest))
                .toList();

        entries = entryRepository.saveAll(entries);

        log.info("Chemical consumption report created: {}", report.getReportNumber());

        return mapper.toResponse(report, entries);

    }

    @Transactional(readOnly = true)
    public List<ChemicalConsumptionResponse> getAll() {

        List<ChemicalConsumptionReport> reports = reportRepository.findAllWithDetails();
        List<Long> reportIds = reports.stream()
                .map(ChemicalConsumptionReport::getId)
                .toList();

        if (reportIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<ChemicalConsumptionEntry>> entriesByReportId = entryRepository
                .findByReportIdIn(reportIds)
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getReport().getId()));

        return reports.stream()
                .map(report -> mapper.toResponse(
                        report,
                        entriesByReportId.getOrDefault(report.getId(), List.of())
                ))
                .toList();

    }

    @Transactional(readOnly = true)
    public ChemicalConsumptionResponse getById(Long id) {

        ChemicalConsumptionReport report = getReport(id);

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public ChemicalConsumptionResponse submit(
            Long id,
            SubmitChemicalConsumptionRequest request
    ) {

        ChemicalConsumptionReport report = getReport(id);

        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be submitted.");
        }

        report.setStatus(ReportStatus.SUBMITTED);

        if (request.getRemarks() != null) {
            report.setRemarks(request.getRemarks());
        }

        report = reportRepository.save(report);

        log.info("Chemical consumption report submitted: {}", report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public ChemicalConsumptionResponse approve(
            Long id,
            ApproveChemicalConsumptionRequest request
    ) {

        return completeApproval(id, request, ReportStatus.APPROVED);

    }

    public ChemicalConsumptionResponse reject(
            Long id,
            ApproveChemicalConsumptionRequest request
    ) {

        return completeApproval(id, request, ReportStatus.REJECTED);

    }

    private ChemicalConsumptionResponse completeApproval(
            Long id,
            ApproveChemicalConsumptionRequest request,
            ReportStatus status
    ) {

        ChemicalConsumptionReport report = getReport(id);
        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted reports can be approved or rejected.");
        }

        report.setStatus(status);
        report.setApprovedBy(currentUser());
        report.setApprovedAt(LocalDateTime.now());

        if (request.getRemarks() != null) {
            report.setRemarks(request.getRemarks());
        }

        report = reportRepository.save(report);

        log.info("Chemical consumption report {} by {}: {}", report.getStatus(), currentUser().getEmployeeId(), report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public void delete(Long id) {

        ChemicalConsumptionReport report = getReport(id);
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be deleted.");
        }

        entryRepository.deleteByReportId(id);
        reportRepository.delete(report);

        log.info("Chemical consumption report deleted: {}", report.getReportNumber());

    }

    private ChemicalConsumptionReport getReport(Long id) {

        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Chemical consumption report not found."));

    }

    private ChemicalConsumptionEntry buildEntry(
            ChemicalConsumptionReport report,
            ChemicalConsumptionEntryRequest request
    ) {

        ParameterMaster parameter = getParameter(request.getParameterId());

        return ChemicalConsumptionEntry.builder()
                .report(report)
                .parameter(parameter)
                .observedValue(request.getObservedValue())
                .inspectionResult(
                        validationService.validate(
                                parameter,
                                request.getObservedValue()
                        )
                )
                .remark(request.getRemark())
                .build();

    }

}
