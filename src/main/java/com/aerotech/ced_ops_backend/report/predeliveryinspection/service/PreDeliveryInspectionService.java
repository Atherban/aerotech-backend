package com.aerotech.ced_ops_backend.report.predeliveryinspection.service;

import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.common.service.ValidationService;
import com.aerotech.ced_ops_backend.common.util.ReportNumberGenerator;
import com.aerotech.ced_ops_backend.master.line.repository.LineRepository;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.master.parameter.repository.ParameterMasterRepository;
import com.aerotech.ced_ops_backend.master.shift.service.ShiftService;
import com.aerotech.ced_ops_backend.notification.service.NotificationChannel;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.ApprovePreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.CreatePreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.request.SubmitPreDeliveryInspectionRequest;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.response.PreDeliveryInspectionResponse;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.entity.PreDeliveryInspectionEntry;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.entity.PreDeliveryInspectionReport;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.mapper.PreDeliveryInspectionMapper;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.repository.PreDeliveryInspectionEntryRepository;
import com.aerotech.ced_ops_backend.report.predeliveryinspection.repository.PreDeliveryInspectionReportRepository;
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
public class PreDeliveryInspectionService
        extends AbstractReportService<PreDeliveryInspectionReport, PreDeliveryInspectionEntry, PreDeliveryInspectionResponse> {

    private final PreDeliveryInspectionReportRepository reportRepository;

    private final PreDeliveryInspectionEntryRepository entryRepository;

    private final PreDeliveryInspectionMapper mapper;

    public PreDeliveryInspectionService(
            ReportNumberGenerator reportNumberGenerator,
            ValidationService validationService,
            ShiftService shiftService,
            LineRepository lineRepository,
            ParameterMasterRepository parameterRepository,
            UserRepository userRepository,
            NotificationChannel notificationChannel,
            PreDeliveryInspectionReportRepository reportRepository,
            PreDeliveryInspectionEntryRepository entryRepository,
            PreDeliveryInspectionMapper mapper
    ) {
        super(reportNumberGenerator, validationService, shiftService, lineRepository, parameterRepository, userRepository, notificationChannel);
        this.reportRepository = reportRepository;
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Override
    protected ReportType reportType() {
        return ReportType.PDI;
    }

    @Override
    protected PreDeliveryInspectionReport getReportOrThrow(Long id) {
        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException(
                                reportLabel() + " report not found."));
    }

    @Override
    protected List<PreDeliveryInspectionReport> findReportsWithDetails() {
        return reportRepository.findAllWithDetails();
    }

    @Override
    protected PreDeliveryInspectionReport saveReport(PreDeliveryInspectionReport report) {
        return reportRepository.save(report);
    }

    @Override
    protected void deleteReport(PreDeliveryInspectionReport report) {
        reportRepository.delete(report);
    }

    @Override
    protected long reportCount() {
        return reportRepository.count();
    }

    @Override
    protected List<PreDeliveryInspectionEntry> entriesOf(PreDeliveryInspectionReport report) {
        return entryRepository.findByReport(report);
    }

    @Override
    protected Map<Long, List<PreDeliveryInspectionEntry>> entriesGroupedByReport(List<Long> reportIds) {
        return entryRepository.findByReportIdIn(reportIds)
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getReport().getId()));
    }

    @Override
    protected List<PreDeliveryInspectionEntry> saveEntries(List<PreDeliveryInspectionEntry> entries) {
        return entryRepository.saveAll(entries);
    }

    @Override
    protected void deleteEntriesByReportId(Long reportId) {
        entryRepository.deleteByReportId(reportId);
    }

    @Override
    protected PreDeliveryInspectionResponse toResponse(
            PreDeliveryInspectionReport report,
            List<PreDeliveryInspectionEntry> entries
    ) {
        return mapper.toResponse(report, entries);
    }

    @Override
    protected PreDeliveryInspectionEntry newEntry(
            PreDeliveryInspectionReport report,
            ParameterMaster parameter,
            String observedValue,
            InspectionResult result,
            String remark
    ) {
        return PreDeliveryInspectionEntry.builder()
                .report(report)
                .parameter(parameter)
                .observedValue(observedValue)
                .inspectionResult(result)
                .remark(remark)
                .build();
    }

    public PreDeliveryInspectionResponse create(CreatePreDeliveryInspectionRequest request) {

        PreDeliveryInspectionReport report = new PreDeliveryInspectionReport();
        report.setProductPartNumber(request.getProductPartNumber());
        report.setBatchNumber(request.getBatchNumber());
        report.setInspectorName(request.getInspectorName());

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

    public List<PreDeliveryInspectionResponse> getAll() {
        return doGetAll();
    }

    public PageResponse<PreDeliveryInspectionResponse> search(ReportFilterRequest filter) {
        return doSearch(filter);
    }

    public PreDeliveryInspectionResponse getById(Long id) {
        return doGetById(id);
    }

    public PreDeliveryInspectionResponse submit(Long id, SubmitPreDeliveryInspectionRequest request) {
        return doSubmit(id, request::getRemarks);
    }

    public PreDeliveryInspectionResponse approve(Long id, ApprovePreDeliveryInspectionRequest request) {
        return doApprove(id, request::getRemarks);
    }

    public PreDeliveryInspectionResponse reject(Long id, ApprovePreDeliveryInspectionRequest request) {
        return doReject(id, request::getRemarks);
    }

    public void delete(Long id) {
        doDelete(id);
    }

}