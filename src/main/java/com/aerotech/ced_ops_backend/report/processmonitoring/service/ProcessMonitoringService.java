package com.aerotech.ced_ops_backend.report.processmonitoring.service;

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
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.ApproveReportRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.CreateProcessMonitoringRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.SubmitReportRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.ProcessMonitoringEntryRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.response.ProcessMonitoringResponse;

import com.aerotech.ced_ops_backend.report.processmonitoring.entity.ProcessMonitoringEntry;
import com.aerotech.ced_ops_backend.report.processmonitoring.entity.ProcessMonitoringReport;
import com.aerotech.ced_ops_backend.report.processmonitoring.mapper.ProcessMonitoringMapper;
import com.aerotech.ced_ops_backend.report.processmonitoring.repository.ProcessMonitoringEntryRepository;
import com.aerotech.ced_ops_backend.report.processmonitoring.repository.ProcessMonitoringReportRepository;
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
public class ProcessMonitoringService {

    private final ProcessMonitoringReportRepository reportRepository;

    private final ProcessMonitoringEntryRepository entryRepository;

    private final ValidationService validationService;

    private final ShiftRepository shiftRepository;

    private final LineRepository lineRepository;

    private final ParameterMasterRepository parameterRepository;

    private final UserRepository userRepository;

    private final ReportNumberGenerator reportNumberGenerator;

    private final ProcessMonitoringMapper mapper;

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
                ReportType.PROCESS_MONITORING,
                sequence
        );

    }

    public ProcessMonitoringResponse create(CreateProcessMonitoringRequest request) {

        ProcessMonitoringReport report = new ProcessMonitoringReport();

        report.setReportNumber(nextReportNumber());
        report.setReportType(ReportType.PROCESS_MONITORING);
        report.setReportDate(request.getReportDate());
        report.setShift(getShift(request.getShiftId()));
        report.setLine(getLine(request.getLineId()));
        report.setStatus(ReportStatus.DRAFT);
        report.setCreatedBy(currentUser());
        report.setRemarks(request.getRemarks());

        report = reportRepository.save(report);

        ProcessMonitoringReport savedReport = report;

        List<ProcessMonitoringEntry> entries = request.getEntries()
                .stream()
                .map(entryRequest -> buildEntry(savedReport, entryRequest))
                .toList();

        entries = entryRepository.saveAll(entries);

        log.info("Process monitoring report created: {}", report.getReportNumber());

        return mapper.toResponse(report, entries);

    }

    @Transactional(readOnly = true)
    public List<ProcessMonitoringResponse> getAll() {

        List<ProcessMonitoringReport> reports = reportRepository.findAllWithDetails();
        List<Long> reportIds = reports.stream()
                .map(ProcessMonitoringReport::getId)
                .toList();

        if (reportIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<ProcessMonitoringEntry>> entriesByReportId = entryRepository
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
    public ProcessMonitoringResponse getById(Long id) {

        ProcessMonitoringReport report = getReport(id);

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public ProcessMonitoringResponse submit(Long id, SubmitReportRequest request) {

        ProcessMonitoringReport report = getReport(id);

        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be submitted.");
        }

        report.setStatus(ReportStatus.SUBMITTED);

        if (request.getRemarks() != null) {
            report.setRemarks(request.getRemarks());
        }

        report = reportRepository.save(report);

        log.info("Process monitoring report submitted: {}", report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public ProcessMonitoringResponse approve(Long id, ApproveReportRequest request) {

        return completeApproval(id, request, ReportStatus.APPROVED);

    }

    public ProcessMonitoringResponse reject(Long id, ApproveReportRequest request) {

        return completeApproval(id, request, ReportStatus.REJECTED);

    }

    private ProcessMonitoringResponse completeApproval(
            Long id,
            ApproveReportRequest request,
            ReportStatus status
    ) {

        ProcessMonitoringReport report = getReport(id);

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

        log.info("Process monitoring report {} by {}: {}", report.getStatus(), currentUser().getEmployeeId(), report.getReportNumber());

        return mapper.toResponse(
                report,
                entryRepository.findByReport(report)
        );

    }

    public void delete(Long id) {

        ProcessMonitoringReport report = getReport(id);

        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be deleted.");
        }

        entryRepository.deleteByReportId(id);
        reportRepository.delete(report);

        log.info("Process monitoring report deleted: {}", report.getReportNumber());

    }

    private ProcessMonitoringReport getReport(Long id) {

        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Process monitoring report not found."));

    }

    private ProcessMonitoringEntry buildEntry(
            ProcessMonitoringReport report,
            ProcessMonitoringEntryRequest request
    ) {

        ParameterMaster parameter = getParameter(request.getParameterId());

        return ProcessMonitoringEntry.builder()
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
