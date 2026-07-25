package com.aerotech.ced_ops_backend.report.predeliveryinspection.service;

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
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.ApprovePreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.CreatePreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.PreDeliveryInspectionEntryRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.SubmitPreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.response.PreDeliveryInspectionResponse;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.entity.PreDeliveryInspectionEntry;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.entity.PreDeliveryInspectionReport;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.mapper.PreDeliveryInspectionMapper;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.repository.PreDeliveryInspectionEntryRepository;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.repository.PreDeliveryInspectionReportRepository;
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
public class PreDeliveryInspectionService {

    private final PreDeliveryInspectionReportRepository reportRepository;

    private final PreDeliveryInspectionEntryRepository entryRepository;

    private final ValidationService validationService;

    private final ShiftRepository shiftRepository;

    private final LineRepository lineRepository;

    private final ParameterMasterRepository parameterRepository;

    private final UserRepository userRepository;

    private final ReportNumberGenerator reportNumberGenerator;

    private final PreDeliveryInspectionMapper mapper;

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
                ReportType.PDI,
                sequence
        );

    }

    public PreDeliveryInspectionResponse create(CreatePreDeliveryInspectionRequest request) {

        PreDeliveryInspectionReport report = new PreDeliveryInspectionReport();
        report.setReportNumber(nextReportNumber());
        report.setReportType(ReportType.PDI);
        report.setReportDate(request.getReportDate());
        report.setShift(getShift(request.getShiftId()));
        report.setLine(getLine(request.getLineId()));
        report.setProductPartNumber(request.getProductPartNumber());
        report.setBatchNumber(request.getBatchNumber());
        report.setInspectorName(request.getInspectorName());
        report.setStatus(ReportStatus.DRAFT);
        report.setCreatedBy(currentUser());
        report.setRemarks(request.getRemarks());

        report = reportRepository.save(report);

        PreDeliveryInspectionReport savedReport = report;

        List<PreDeliveryInspectionEntry> entries = request.getEntries()
                .stream()
                .map(entryRequest -> buildEntry(savedReport, entryRequest))
                .toList();

        entries = entryRepository.saveAll(entries);

        log.info("Pre-delivery inspection report created: {}", report.getReportNumber());

        return mapper.toResponse(report, entries);

    }

    @Transactional(readOnly = true)
    public List<PreDeliveryInspectionResponse> getAll() {

        List<PreDeliveryInspectionReport> reports = reportRepository.findAllWithDetails();
        List<Long> reportIds = reports.stream()
                .map(PreDeliveryInspectionReport::getId)
                .toList();

        if (reportIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<PreDeliveryInspectionEntry>> entriesByReportId = entryRepository
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
    public PreDeliveryInspectionResponse getById(Long id) {

        PreDeliveryInspectionReport report = getReport(id);

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public PreDeliveryInspectionResponse submit(Long id, SubmitPreDeliveryInspectionRequest request) {

        PreDeliveryInspectionReport report = getReport(id);

        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be submitted.");
        }

        report.setStatus(ReportStatus.SUBMITTED);

        if (request.getRemarks() != null) {
            report.setRemarks(request.getRemarks());
        }

        report = reportRepository.save(report);

        log.info("Pre-delivery inspection report submitted: {}", report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public PreDeliveryInspectionResponse approve(Long id, ApprovePreDeliveryInspectionRequest request) {

        return completeApproval(id, request, ReportStatus.APPROVED);

    }

    public PreDeliveryInspectionResponse reject(Long id, ApprovePreDeliveryInspectionRequest request) {

        return completeApproval(id, request, ReportStatus.REJECTED);

    }

    public void delete(Long id) {

        PreDeliveryInspectionReport report = getReport(id);
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be deleted.");
        }

        entryRepository.deleteByReportId(id);
        reportRepository.delete(report);

        log.info("Pre-delivery inspection report deleted: {}", report.getReportNumber());

    }

    private PreDeliveryInspectionResponse completeApproval(
            Long id,
            ApprovePreDeliveryInspectionRequest request,
            ReportStatus status
    ) {

        PreDeliveryInspectionReport report = getReport(id);
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

        log.info("Pre-delivery inspection report {} by {}: {}", report.getStatus(), currentUser().getEmployeeId(), report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    private PreDeliveryInspectionReport getReport(Long id) {

        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pre-delivery inspection report not found."));

    }

    private PreDeliveryInspectionEntry buildEntry(
            PreDeliveryInspectionReport report,
            PreDeliveryInspectionEntryRequest request
    ) {

        ParameterMaster parameter = getParameter(request.getParameterId());

        return PreDeliveryInspectionEntry.builder()
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
