package com.aerotech.ced_ops_backend.report.dailystartup.service;

import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.common.service.ValidationService;
import com.aerotech.ced_ops_backend.common.util.ReportNumberGenerator;
import com.aerotech.ced_ops_backend.master.line.repository.LineRepository;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.master.parameter.repository.ParameterMasterRepository;
import com.aerotech.ced_ops_backend.master.shift.service.ShiftService;
import com.aerotech.ced_ops_backend.notification.service.NotificationChannel;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.request.ApproveDailyStartupRequest;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.request.CreateDailyStartupRequest;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.request.SubmitDailyStartupRequest;
import com.aerotech.ced_ops_backend.report.dailystartup.dto.response.DailyStartupResponse;
import com.aerotech.ced_ops_backend.report.dailystartup.entity.DailyStartupEntry;
import com.aerotech.ced_ops_backend.report.dailystartup.entity.DailyStartupReport;
import com.aerotech.ced_ops_backend.report.dailystartup.mapper.DailyStartupMapper;
import com.aerotech.ced_ops_backend.report.dailystartup.repository.DailyStartupEntryRepository;
import com.aerotech.ced_ops_backend.report.dailystartup.repository.DailyStartupReportRepository;
import com.aerotech.ced_ops_backend.report.support.AbstractReportService;
import com.aerotech.ced_ops_backend.report.support.ReportFilterRequest;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class DailyStartupService
        extends AbstractReportService<DailyStartupReport, DailyStartupEntry, DailyStartupResponse> {

    private final DailyStartupReportRepository reportRepository;

    private final DailyStartupEntryRepository entryRepository;

    private final DailyStartupMapper mapper;

    public DailyStartupService(
            ReportNumberGenerator reportNumberGenerator,
            ValidationService validationService,
            ShiftService shiftService,
            LineRepository lineRepository,
            ParameterMasterRepository parameterRepository,
            UserRepository userRepository,
            NotificationChannel notificationChannel,
            DailyStartupReportRepository reportRepository,
            DailyStartupEntryRepository entryRepository,
            DailyStartupMapper mapper
    ) {
        super(reportNumberGenerator, validationService, shiftService, lineRepository, parameterRepository, userRepository, notificationChannel);
        this.reportRepository = reportRepository;
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Override
    protected ReportType reportType() {
        return ReportType.DAILY_STARTUP;
    }

    @Override
    protected DailyStartupReport getReportOrThrow(Long id) {
        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException(
                                reportLabel() + " report not found."));
    }

    @Override
    protected List<DailyStartupReport> findReportsWithDetails() {
        return reportRepository.findAllWithDetails();
    }

    @Override
    protected DailyStartupReport saveReport(DailyStartupReport report) {
        return reportRepository.save(report);
    }

    @Override
    protected void deleteReport(DailyStartupReport report) {
        reportRepository.delete(report);
    }

    @Override
    protected long reportCount() {
        return reportRepository.count();
    }

    @Override
    protected List<DailyStartupEntry> entriesOf(DailyStartupReport report) {
        return entryRepository.findByReport(report);
    }

    @Override
    protected Map<Long, List<DailyStartupEntry>> entriesGroupedByReport(List<Long> reportIds) {
        return entryRepository.findByReportIdIn(reportIds)
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getReport().getId()));
    }

    @Override
    protected List<DailyStartupEntry> saveEntries(List<DailyStartupEntry> entries) {
        return entryRepository.saveAll(entries);
    }

    @Override
    protected void deleteEntriesByReportId(Long reportId) {
        entryRepository.deleteByReportId(reportId);
    }

    @Override
    protected DailyStartupResponse toResponse(DailyStartupReport report, List<DailyStartupEntry> entries) {
        return mapper.toResponse(report, entries);
    }

    @Override
    protected DailyStartupEntry newEntry(
            DailyStartupReport report,
            ParameterMaster parameter,
            String observedValue,
            InspectionResult result,
            String remark
    ) {
        return DailyStartupEntry.builder()
                .report(report)
                .parameter(parameter)
                .observedValue(observedValue)
                .inspectionResult(result)
                .remark(remark)
                .build();
    }

    public DailyStartupResponse create(CreateDailyStartupRequest request) {

        DailyStartupReport report = new DailyStartupReport();

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

    public List<DailyStartupResponse> getAll() {
        return doGetAll();
    }

    public PageResponse<DailyStartupResponse> search(ReportFilterRequest filter) {
        return doSearch(filter);
    }

    public DailyStartupResponse getById(Long id) {
        return doGetById(id);
    }

    public DailyStartupResponse submit(Long id, SubmitDailyStartupRequest request) {
        return doSubmit(id, request::getRemarks);
    }

    public DailyStartupResponse approve(Long id, ApproveDailyStartupRequest request) {
        return doApprove(id, request::getRemarks);
    }

    public DailyStartupResponse reject(Long id, ApproveDailyStartupRequest request) {
        return doReject(id, request::getRemarks);
    }

    public void delete(Long id) {
        doDelete(id);
    }

}