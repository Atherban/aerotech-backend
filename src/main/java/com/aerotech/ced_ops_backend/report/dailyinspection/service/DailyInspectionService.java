package com.aerotech.ced_ops_backend.report.dailyinspection.service;

import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.common.service.ValidationService;
import com.aerotech.ced_ops_backend.common.util.ReportNumberGenerator;
import com.aerotech.ced_ops_backend.master.line.repository.LineRepository;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.master.parameter.repository.ParameterMasterRepository;
import com.aerotech.ced_ops_backend.master.shift.service.ShiftService;
import com.aerotech.ced_ops_backend.notification.service.NotificationChannel;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.ApproveDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.CreateDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.request.SubmitDailyInspectionRequest;
import com.aerotech.ced_ops_backend.report.dailyinspection.dto.response.DailyInspectionResponse;
import com.aerotech.ced_ops_backend.report.dailyinspection.entity.DailyInspectionEntry;
import com.aerotech.ced_ops_backend.report.dailyinspection.entity.DailyInspectionReport;
import com.aerotech.ced_ops_backend.report.dailyinspection.mapper.DailyInspectionMapper;
import com.aerotech.ced_ops_backend.report.dailyinspection.repository.DailyInspectionEntryRepository;
import com.aerotech.ced_ops_backend.report.dailyinspection.repository.DailyInspectionReportRepository;
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
public class DailyInspectionService
        extends AbstractReportService<DailyInspectionReport, DailyInspectionEntry, DailyInspectionResponse> {

    private final DailyInspectionReportRepository reportRepository;

    private final DailyInspectionEntryRepository entryRepository;

    private final DailyInspectionMapper mapper;

    public DailyInspectionService(
            ReportNumberGenerator reportNumberGenerator,
            ValidationService validationService,
            ShiftService shiftService,
            LineRepository lineRepository,
            ParameterMasterRepository parameterRepository,
            UserRepository userRepository,
            NotificationChannel notificationChannel,
            DailyInspectionReportRepository reportRepository,
            DailyInspectionEntryRepository entryRepository,
            DailyInspectionMapper mapper
    ) {
        super(reportNumberGenerator, validationService, shiftService, lineRepository, parameterRepository, userRepository, notificationChannel);
        this.reportRepository = reportRepository;
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Override
    protected ReportType reportType() {
        return ReportType.DAILY_INSPECTION;
    }

    @Override
    protected DailyInspectionReport getReportOrThrow(Long id) {
        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException(
                                reportLabel() + " report not found."));
    }

    @Override
    protected List<DailyInspectionReport> findReportsWithDetails() {
        return reportRepository.findAllWithDetails();
    }

    @Override
    protected DailyInspectionReport saveReport(DailyInspectionReport report) {
        return reportRepository.save(report);
    }

    @Override
    protected void deleteReport(DailyInspectionReport report) {
        reportRepository.delete(report);
    }

    @Override
    protected long reportCount() {
        return reportRepository.count();
    }

    @Override
    protected List<DailyInspectionEntry> entriesOf(DailyInspectionReport report) {
        return entryRepository.findByReport(report);
    }

    @Override
    protected Map<Long, List<DailyInspectionEntry>> entriesGroupedByReport(List<Long> reportIds) {
        return entryRepository.findByReportIdIn(reportIds)
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getReport().getId()));
    }

    @Override
    protected List<DailyInspectionEntry> saveEntries(List<DailyInspectionEntry> entries) {
        return entryRepository.saveAll(entries);
    }

    @Override
    protected void deleteEntriesByReportId(Long reportId) {
        entryRepository.deleteByReportId(reportId);
    }

    @Override
    protected DailyInspectionResponse toResponse(DailyInspectionReport report, List<DailyInspectionEntry> entries) {
        return mapper.toResponse(report, entries);
    }

    @Override
    protected DailyInspectionEntry newEntry(
            DailyInspectionReport report,
            ParameterMaster parameter,
            String observedValue,
            InspectionResult result,
            String remark
    ) {
        return DailyInspectionEntry.builder()
                .report(report)
                .parameter(parameter)
                .observedValue(observedValue)
                .inspectionResult(result)
                .remark(remark)
                .build();
    }

    public DailyInspectionResponse create(CreateDailyInspectionRequest request) {

        DailyInspectionReport report = new DailyInspectionReport();
        report.setInspectorName(request.getInspectorName());
        report.setCorrectiveAction(request.getCorrectiveAction());

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

    public List<DailyInspectionResponse> getAll() {
        return doGetAll();
    }

    public PageResponse<DailyInspectionResponse> search(ReportFilterRequest filter) {
        return doSearch(filter);
    }

    public DailyInspectionResponse getById(Long id) {
        return doGetById(id);
    }

    public DailyInspectionResponse submit(Long id, SubmitDailyInspectionRequest request) {
        return doSubmit(id, request::getRemarks);
    }

    public DailyInspectionResponse approve(Long id, ApproveDailyInspectionRequest request) {
        return doApprove(id, request::getRemarks);
    }

    public DailyInspectionResponse reject(Long id, ApproveDailyInspectionRequest request) {
        return doReject(id, request::getRemarks);
    }

    public void delete(Long id) {
        doDelete(id);
    }

}