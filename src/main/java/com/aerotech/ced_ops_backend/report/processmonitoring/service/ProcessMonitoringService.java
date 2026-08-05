package com.aerotech.ced_ops_backend.report.processmonitoring.service;

import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.common.service.ValidationService;
import com.aerotech.ced_ops_backend.common.util.ReportNumberGenerator;
import com.aerotech.ced_ops_backend.master.line.repository.LineRepository;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.master.parameter.repository.ParameterMasterRepository;
import com.aerotech.ced_ops_backend.master.shift.service.ShiftService;
import com.aerotech.ced_ops_backend.notification.service.NotificationChannel;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.ApproveReportRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.CreateProcessMonitoringRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.request.SubmitReportRequest;
import com.aerotech.ced_ops_backend.report.processmonitoring.dto.response.ProcessMonitoringResponse;
import com.aerotech.ced_ops_backend.report.processmonitoring.entity.ProcessMonitoringEntry;
import com.aerotech.ced_ops_backend.report.processmonitoring.entity.ProcessMonitoringReport;
import com.aerotech.ced_ops_backend.report.processmonitoring.mapper.ProcessMonitoringMapper;
import com.aerotech.ced_ops_backend.report.processmonitoring.repository.ProcessMonitoringEntryRepository;
import com.aerotech.ced_ops_backend.report.processmonitoring.repository.ProcessMonitoringReportRepository;
import com.aerotech.ced_ops_backend.report.support.AbstractReportService;
import com.aerotech.ced_ops_backend.report.support.ReportFilterRequest;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProcessMonitoringService
        extends AbstractReportService<ProcessMonitoringReport, ProcessMonitoringEntry, ProcessMonitoringResponse> {

    private final ProcessMonitoringReportRepository reportRepository;

    private final ProcessMonitoringEntryRepository entryRepository;

    private final ProcessMonitoringMapper mapper;

    public ProcessMonitoringService(
            ReportNumberGenerator reportNumberGenerator,
            ValidationService validationService,
            ShiftService shiftService,
            LineRepository lineRepository,
            ParameterMasterRepository parameterRepository,
            UserRepository userRepository,
            NotificationChannel notificationChannel,
            ProcessMonitoringReportRepository reportRepository,
            ProcessMonitoringEntryRepository entryRepository,
            ProcessMonitoringMapper mapper
    ) {
        super(reportNumberGenerator, validationService, shiftService, lineRepository, parameterRepository, userRepository, notificationChannel);
        this.reportRepository = reportRepository;
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Override
    protected ReportType reportType() {
        return ReportType.PROCESS_MONITORING;
    }

    @Override
    protected ProcessMonitoringReport getReportOrThrow(Long id) {
        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException(
                                reportLabel() + " report not found."));
    }

    @Override
    protected List<ProcessMonitoringReport> findReportsWithDetails() {
        return reportRepository.findAllWithDetails();
    }

    @Override
    protected ProcessMonitoringReport saveReport(ProcessMonitoringReport report) {
        return reportRepository.save(report);
    }

    @Override
    protected void deleteReport(ProcessMonitoringReport report) {
        reportRepository.delete(report);
    }

    @Override
    protected long reportCount() {
        return reportRepository.count();
    }

    @Override
    protected List<ProcessMonitoringEntry> entriesOf(ProcessMonitoringReport report) {
        return entryRepository.findByReport(report);
    }

    @Override
    protected Map<Long, List<ProcessMonitoringEntry>> entriesGroupedByReport(List<Long> reportIds) {
        return entryRepository.findByReportIdIn(reportIds)
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getReport().getId()));
    }

    @Override
    protected List<ProcessMonitoringEntry> saveEntries(List<ProcessMonitoringEntry> entries) {
        return entryRepository.saveAll(entries);
    }

    @Override
    protected void deleteEntriesByReportId(Long reportId) {
        entryRepository.deleteByReportId(reportId);
    }

    @Override
    protected ProcessMonitoringResponse toResponse(ProcessMonitoringReport report, List<ProcessMonitoringEntry> entries) {
        return mapper.toResponse(report, entries);
    }

    @Override
    protected ProcessMonitoringEntry newEntry(
            ProcessMonitoringReport report,
            ParameterMaster parameter,
            String observedValue,
            InspectionResult result,
            String remark
    ) {
        return ProcessMonitoringEntry.builder()
                .report(report)
                .parameter(parameter)
                .observedValue(observedValue)
                .inspectionResult(result)
                .remark(remark)
                .build();
    }

    public ProcessMonitoringResponse create(CreateProcessMonitoringRequest request) {

        ProcessMonitoringReport report = new ProcessMonitoringReport();

        return doCreateReport(
                report,
                request.getReportDate(),
                request.getShiftId(),
                request.getLineId(),
                request.getRemarks(),
                savedReport -> request.getEntries()
                        .stream()
                        .map(entry -> buildEntry(
                                savedReport,
                                entry.getParameterId(),
                                entry.getObservedValue(),
                                entry.getRemark()))
                        .toList()
        );
    }

    public List<ProcessMonitoringResponse> getAll() {
        return doGetAll();
    }

    public PageResponse<ProcessMonitoringResponse> search(ReportFilterRequest filter) {
        return doSearch(filter);
    }

    public ProcessMonitoringResponse getById(Long id) {
        return doGetById(id);
    }

    public ProcessMonitoringResponse submit(Long id, SubmitReportRequest request) {
        return doSubmit(id, request::getRemarks);
    }

    public ProcessMonitoringResponse approve(Long id, ApproveReportRequest request) {
        return doApprove(id, request::getRemarks);
    }

    public ProcessMonitoringResponse reject(Long id, ApproveReportRequest request) {
        return doReject(id, request::getRemarks);
    }

    public void delete(Long id) {
        doDelete(id);
    }

}