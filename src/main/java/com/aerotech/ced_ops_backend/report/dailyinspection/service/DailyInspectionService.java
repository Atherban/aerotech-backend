package com.aerotech.ced_ops_backend.report.dailyinspection.service;

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
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.ApproveDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.CreateDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.DailyInspectionEntryRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.SubmitDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.response.DailyInspectionResponse;
import com.aerotech.ced_ops_backend.report.dailyinspection.entity.DailyInspectionEntry;
import com.aerotech.ced_ops_backend.report.dailyinspection.entity.DailyInspectionReport;
import com.aerotech.ced_ops_backend.report.dailyinspection.mapper.DailyInspectionMapper;
import com.aerotech.ced_ops_backend.report.dailyinspection.repository.DailyInspectionEntryRepository;
import com.aerotech.ced_ops_backend.report.dailyinspection.repository.DailyInspectionReportRepository;
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
public class DailyInspectionService {

    private final DailyInspectionReportRepository reportRepository;

    private final DailyInspectionEntryRepository entryRepository;

    private final ValidationService validationService;

    private final ShiftRepository shiftRepository;

    private final LineRepository lineRepository;

    private final ProcessMasterRepository processRepository;

    private final ParameterMasterRepository parameterRepository;

    private final UserRepository userRepository;

    private final ReportNumberGenerator reportNumberGenerator;

    private final DailyInspectionMapper mapper;

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
                ReportType.DAILY_INSPECTION,
                sequence
        );

    }

    public DailyInspectionResponse create(CreateDailyInspectionRequest request) {

        DailyInspectionReport report = new DailyInspectionReport();
        report.setReportNumber(nextReportNumber());
        report.setReportType(ReportType.DAILY_INSPECTION);
        report.setReportDate(request.getReportDate());
        report.setShift(getShift(request.getShiftId()));
        report.setLine(getLine(request.getLineId()));
        report.setProcess(getProcess(request.getProcessId()));
        report.setInspectorName(request.getInspectorName());
        report.setCorrectiveAction(request.getCorrectiveAction());
        report.setStatus(ReportStatus.DRAFT);
        report.setCreatedBy(currentUser());
        report.setRemarks(request.getRemarks());

        report = reportRepository.save(report);

        DailyInspectionReport savedReport = report;

        List<DailyInspectionEntry> entries = request.getEntries()
                .stream()
                .map(entryRequest -> buildEntry(savedReport, entryRequest))
                .toList();

        entries = entryRepository.saveAll(entries);

        log.info("Daily inspection report created: {}", report.getReportNumber());

        return mapper.toResponse(report, entries);

    }

    @Transactional(readOnly = true)
    public List<DailyInspectionResponse> getAll() {

        List<DailyInspectionReport> reports = reportRepository.findAllWithDetails();
        List<Long> reportIds = reports.stream()
                .map(DailyInspectionReport::getId)
                .toList();

        if (reportIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<DailyInspectionEntry>> entriesByReportId = entryRepository
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
    public DailyInspectionResponse getById(Long id) {

        DailyInspectionReport report = getReport(id);

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public DailyInspectionResponse submit(Long id, SubmitDailyInspectionRequest request) {

        DailyInspectionReport report = getReport(id);

        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be submitted.");
        }

        report.setStatus(ReportStatus.SUBMITTED);

        if (request.getRemarks() != null) {
            report.setRemarks(request.getRemarks());
        }

        report = reportRepository.save(report);

        log.info("Daily inspection report submitted: {}", report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public DailyInspectionResponse approve(Long id, ApproveDailyInspectionRequest request) {

        return completeApproval(id, request, ReportStatus.APPROVED);

    }

    public DailyInspectionResponse reject(Long id, ApproveDailyInspectionRequest request) {

        return completeApproval(id, request, ReportStatus.REJECTED);

    }

    public void delete(Long id) {

        DailyInspectionReport report = getReport(id);
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be deleted.");
        }

        entryRepository.deleteByReportId(id);
        reportRepository.delete(report);

        log.info("Daily inspection report deleted: {}", report.getReportNumber());

    }

    private DailyInspectionResponse completeApproval(
            Long id,
            ApproveDailyInspectionRequest request,
            ReportStatus status
    ) {

        DailyInspectionReport report = getReport(id);
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

        log.info("Daily inspection report {} by {}: {}", report.getStatus(), currentUser().getEmployeeId(), report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    private DailyInspectionReport getReport(Long id) {

        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Daily inspection report not found."));

    }

    private DailyInspectionEntry buildEntry(
            DailyInspectionReport report,
            DailyInspectionEntryRequest request
    ) {

        ParameterMaster parameter = getParameter(request.getParameterId());

        return DailyInspectionEntry.builder()
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
