package com.aerotech.ced_ops_backend.report.dailystartup.service;

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
import com.aerotech.ced_ops_backend.report.dailystartup.dto.request.ApproveDailyStartupRequest;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.request.CreateDailyStartupRequest;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.request.DailyStartupEntryRequest;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.request.SubmitDailyStartupRequest;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.response.DailyStartupResponse;
import com.aerotech.ced_ops_backend.report.dailystartup.entity.DailyStartupEntry;
import com.aerotech.ced_ops_backend.report.dailystartup.entity.DailyStartupReport;
import com.aerotech.ced_ops_backend.report.dailystartup.mapper.DailyStartupMapper;
import com.aerotech.ced_ops_backend.report.dailystartup.repository.DailyStartupEntryRepository;
import com.aerotech.ced_ops_backend.report.dailystartup.repository.DailyStartupReportRepository;
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
public class DailyStartupService {

    private final DailyStartupReportRepository reportRepository;

    private final DailyStartupEntryRepository entryRepository;

    private final ValidationService validationService;

    private final ShiftRepository shiftRepository;

    private final LineRepository lineRepository;

    private final ParameterMasterRepository parameterRepository;

    private final UserRepository userRepository;

    private final ReportNumberGenerator reportNumberGenerator;

    private final DailyStartupMapper mapper;

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
                ReportType.DAILY_STARTUP,
                sequence
        );

    }

    public DailyStartupResponse create(CreateDailyStartupRequest request) {

        DailyStartupReport report = new DailyStartupReport();
        report.setReportNumber(nextReportNumber());
        report.setReportType(ReportType.DAILY_STARTUP);
        report.setReportDate(request.getReportDate());
        report.setShift(getShift(request.getShiftId()));
        report.setLine(getLine(request.getLineId()));
        report.setStatus(ReportStatus.DRAFT);
        report.setCreatedBy(currentUser());
        report.setRemarks(request.getRemarks());

        report = reportRepository.save(report);

        DailyStartupReport savedReport = report;

        List<DailyStartupEntry> entries = request.getEntries()
                .stream()
                .map(entryRequest -> buildEntry(savedReport, entryRequest))
                .toList();

        entries = entryRepository.saveAll(entries);

        log.info("Daily startup report created: {}", report.getReportNumber());

        return mapper.toResponse(report, entries);

    }

    @Transactional(readOnly = true)
    public List<DailyStartupResponse> getAll() {

        List<DailyStartupReport> reports = reportRepository.findAllWithDetails();
        List<Long> reportIds = reports.stream()
                .map(DailyStartupReport::getId)
                .toList();

        if (reportIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<DailyStartupEntry>> entriesByReportId = entryRepository
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
    public DailyStartupResponse getById(Long id) {

        DailyStartupReport report = getReport(id);

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public DailyStartupResponse submit(Long id, SubmitDailyStartupRequest request) {

        DailyStartupReport report = getReport(id);

        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be submitted.");
        }

        report.setStatus(ReportStatus.SUBMITTED);

        if (request.getRemarks() != null) {
            report.setRemarks(request.getRemarks());
        }

        report = reportRepository.save(report);

        log.info("Daily startup report submitted: {}", report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public DailyStartupResponse approve(Long id, ApproveDailyStartupRequest request) {

        return completeApproval(id, request, ReportStatus.APPROVED);

    }

    public DailyStartupResponse reject(Long id, ApproveDailyStartupRequest request) {

        return completeApproval(id, request, ReportStatus.REJECTED);

    }

    public void delete(Long id) {

        DailyStartupReport report = getReport(id);
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be deleted.");
        }

        entryRepository.deleteByReportId(id);
        reportRepository.delete(report);

        log.info("Daily startup report deleted: {}", report.getReportNumber());

    }

    private DailyStartupResponse completeApproval(
            Long id,
            ApproveDailyStartupRequest request,
            ReportStatus status
    ) {

        DailyStartupReport report = getReport(id);
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

        log.info("Daily startup report {} by {}: {}", report.getStatus(), currentUser().getEmployeeId(), report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    private DailyStartupReport getReport(Long id) {

        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Daily startup report not found."));

    }

    private DailyStartupEntry buildEntry(
            DailyStartupReport report,
            DailyStartupEntryRequest request
    ) {

        ParameterMaster parameter = getParameter(request.getParameterId());

        return DailyStartupEntry.builder()
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
