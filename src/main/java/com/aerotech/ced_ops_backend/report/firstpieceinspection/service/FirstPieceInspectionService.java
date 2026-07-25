package com.aerotech.ced_ops_backend.report.firstpieceinspection.service;

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
import com.aerotech.ced_ops_backend.master.process.entity.ProcessMaster;
import com.aerotech.ced_ops_backend.master.process.repository.ProcessMasterRepository;
import com.aerotech.ced_ops_backend.master.shift.entity.Shift;
import com.aerotech.ced_ops_backend.master.shift.repository.ShiftRepository;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.ApproveFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.CreateFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.FirstPieceInspectionEntryRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.SubmitFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.response.FirstPieceInspectionResponse;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.entity.FirstPieceInspectionEntry;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.entity.FirstPieceInspectionReport;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.mapper.FirstPieceInspectionMapper;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.repository.FirstPieceInspectionEntryRepository;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.repository.FirstPieceInspectionReportRepository;
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
public class FirstPieceInspectionService {

    private final FirstPieceInspectionReportRepository reportRepository;

    private final FirstPieceInspectionEntryRepository entryRepository;

    private final ValidationService validationService;

    private final ShiftRepository shiftRepository;

    private final LineRepository lineRepository;

    private final ProcessMasterRepository processRepository;

    private final ParameterMasterRepository parameterRepository;

    private final UserRepository userRepository;

    private final ReportNumberGenerator reportNumberGenerator;

    private final FirstPieceInspectionMapper mapper;

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

    private ProcessMaster getProcess(Long id) {

        return processRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Process not found."));

    }

    private ParameterMaster getParameter(Long id) {

        return parameterRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Parameter not found."));

    }

    private String nextReportNumber() {

        long sequence = reportRepository.count() + 1;

        return reportNumberGenerator.generate(
                ReportType.FIRST_PIECE_INSPECTION,
                sequence
        );

    }

    public FirstPieceInspectionResponse create(CreateFirstPieceInspectionRequest request) {

        FirstPieceInspectionReport report = new FirstPieceInspectionReport();
        report.setReportNumber(nextReportNumber());
        report.setReportType(ReportType.FIRST_PIECE_INSPECTION);
        report.setReportDate(request.getReportDate());
        report.setShift(getShift(request.getShiftId()));
        report.setLine(getLine(request.getLineId()));
        report.setProcess(getProcess(request.getProcessId()));
        report.setProductCastingNumber(request.getProductCastingNumber());
        report.setOperatorName(request.getOperatorName());
        report.setInspectorName(request.getInspectorName());
        report.setStatus(ReportStatus.DRAFT);
        report.setCreatedBy(currentUser());
        report.setRemarks(request.getRemarks());

        report = reportRepository.save(report);

        FirstPieceInspectionReport savedReport = report;

        List<FirstPieceInspectionEntry> entries = request.getEntries()
                .stream()
                .map(entryRequest -> buildEntry(savedReport, entryRequest))
                .toList();

        entries = entryRepository.saveAll(entries);

        log.info("First piece inspection report created: {}", report.getReportNumber());

        return mapper.toResponse(report, entries);

    }

    @Transactional(readOnly = true)
    public List<FirstPieceInspectionResponse> getAll() {

        List<FirstPieceInspectionReport> reports = reportRepository.findAllWithDetails();
        List<Long> reportIds = reports.stream()
                .map(FirstPieceInspectionReport::getId)
                .toList();

        if (reportIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<FirstPieceInspectionEntry>> entriesByReportId = entryRepository
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
    public FirstPieceInspectionResponse getById(Long id) {

        FirstPieceInspectionReport report = getReport(id);

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public FirstPieceInspectionResponse submit(Long id, SubmitFirstPieceInspectionRequest request) {

        FirstPieceInspectionReport report = getReport(id);

        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be submitted.");
        }

        report.setStatus(ReportStatus.SUBMITTED);

        if (request.getRemarks() != null) {
            report.setRemarks(request.getRemarks());
        }

        report = reportRepository.save(report);

        log.info("First piece inspection report submitted: {}", report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public FirstPieceInspectionResponse approve(Long id, ApproveFirstPieceInspectionRequest request) {

        return completeApproval(id, request, ReportStatus.APPROVED);

    }

    public FirstPieceInspectionResponse reject(Long id, ApproveFirstPieceInspectionRequest request) {

        return completeApproval(id, request, ReportStatus.REJECTED);

    }

    public void delete(Long id) {

        FirstPieceInspectionReport report = getReport(id);
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be deleted.");
        }

        entryRepository.deleteByReportId(id);
        reportRepository.delete(report);

        log.info("First piece inspection report deleted: {}", report.getReportNumber());

    }

    private FirstPieceInspectionResponse completeApproval(
            Long id,
            ApproveFirstPieceInspectionRequest request,
            ReportStatus status
    ) {

        FirstPieceInspectionReport report = getReport(id);
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

        log.info("First piece inspection report {} by {}: {}", report.getStatus(), currentUser().getEmployeeId(), report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    private FirstPieceInspectionReport getReport(Long id) {

        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("First piece inspection report not found."));

    }

    private FirstPieceInspectionEntry buildEntry(
            FirstPieceInspectionReport report,
            FirstPieceInspectionEntryRequest request
    ) {

        ParameterMaster parameter = getParameter(request.getParameterId());

        return FirstPieceInspectionEntry.builder()
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
