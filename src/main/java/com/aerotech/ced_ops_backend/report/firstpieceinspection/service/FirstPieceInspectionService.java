package com.aerotech.ced_ops_backend.report.firstpieceinspection.service;

import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.common.service.ValidationService;
import com.aerotech.ced_ops_backend.common.util.ReportNumberGenerator;
import com.aerotech.ced_ops_backend.master.line.repository.LineRepository;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.master.parameter.repository.ParameterMasterRepository;
import com.aerotech.ced_ops_backend.master.shift.service.ShiftService;
import com.aerotech.ced_ops_backend.notification.service.NotificationChannel;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.ApproveFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.CreateFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.request.SubmitFirstPieceInspectionRequest;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.response.FirstPieceInspectionResponse;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.entity.FirstPieceInspectionEntry;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.entity.FirstPieceInspectionReport;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.mapper.FirstPieceInspectionMapper;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.repository.FirstPieceInspectionEntryRepository;
import com.aerotech.ced_ops_backend.report.firstpieceinspection.repository.FirstPieceInspectionReportRepository;
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
public class FirstPieceInspectionService
        extends AbstractReportService<FirstPieceInspectionReport, FirstPieceInspectionEntry, FirstPieceInspectionResponse> {

    private final FirstPieceInspectionReportRepository reportRepository;

    private final FirstPieceInspectionEntryRepository entryRepository;

    private final FirstPieceInspectionMapper mapper;

    public FirstPieceInspectionService(
            ReportNumberGenerator reportNumberGenerator,
            ValidationService validationService,
            ShiftService shiftService,
            LineRepository lineRepository,
            ParameterMasterRepository parameterRepository,
            UserRepository userRepository,
            NotificationChannel notificationChannel,
            FirstPieceInspectionReportRepository reportRepository,
            FirstPieceInspectionEntryRepository entryRepository,
            FirstPieceInspectionMapper mapper
    ) {
        super(reportNumberGenerator, validationService, shiftService, lineRepository, parameterRepository, userRepository, notificationChannel);
        this.reportRepository = reportRepository;
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Override
    protected ReportType reportType() {
        return ReportType.FIRST_PIECE_INSPECTION;
    }

    @Override
    protected FirstPieceInspectionReport getReportOrThrow(Long id) {
        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException(
                                reportLabel() + " report not found."));
    }

    @Override
    protected List<FirstPieceInspectionReport> findReportsWithDetails() {
        return reportRepository.findAllWithDetails();
    }

    @Override
    protected FirstPieceInspectionReport saveReport(FirstPieceInspectionReport report) {
        return reportRepository.save(report);
    }

    @Override
    protected void deleteReport(FirstPieceInspectionReport report) {
        reportRepository.delete(report);
    }

    @Override
    protected long reportCount() {
        return reportRepository.count();
    }

    @Override
    protected List<FirstPieceInspectionEntry> entriesOf(FirstPieceInspectionReport report) {
        return entryRepository.findByReport(report);
    }

    @Override
    protected Map<Long, List<FirstPieceInspectionEntry>> entriesGroupedByReport(List<Long> reportIds) {
        return entryRepository.findByReportIdIn(reportIds)
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getReport().getId()));
    }

    @Override
    protected List<FirstPieceInspectionEntry> saveEntries(List<FirstPieceInspectionEntry> entries) {
        return entryRepository.saveAll(entries);
    }

    @Override
    protected void deleteEntriesByReportId(Long reportId) {
        entryRepository.deleteByReportId(reportId);
    }

    @Override
    protected FirstPieceInspectionResponse toResponse(
            FirstPieceInspectionReport report,
            List<FirstPieceInspectionEntry> entries
    ) {
        return mapper.toResponse(report, entries);
    }

    @Override
    protected FirstPieceInspectionEntry newEntry(
            FirstPieceInspectionReport report,
            ParameterMaster parameter,
            String observedValue,
            InspectionResult result,
            String remark
    ) {
        return FirstPieceInspectionEntry.builder()
                .report(report)
                .parameter(parameter)
                .observedValue(observedValue)
                .inspectionResult(result)
                .remark(remark)
                .build();
    }

    public FirstPieceInspectionResponse create(CreateFirstPieceInspectionRequest request) {

        FirstPieceInspectionReport report = new FirstPieceInspectionReport();
        report.setProductCastingNumber(request.getProductCastingNumber());
        report.setOperatorName(request.getOperatorName());
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

    public List<FirstPieceInspectionResponse> getAll() {
        return doGetAll();
    }

    public PageResponse<FirstPieceInspectionResponse> search(ReportFilterRequest filter) {
        return doSearch(filter);
    }

    public FirstPieceInspectionResponse getById(Long id) {
        return doGetById(id);
    }

    public FirstPieceInspectionResponse submit(Long id, SubmitFirstPieceInspectionRequest request) {
        return doSubmit(id, request::getRemarks);
    }

    public FirstPieceInspectionResponse approve(Long id, ApproveFirstPieceInspectionRequest request) {
        return doApprove(id, request::getRemarks);
    }

    public FirstPieceInspectionResponse reject(Long id, ApproveFirstPieceInspectionRequest request) {
        return doReject(id, request::getRemarks);
    }

    public void delete(Long id) {
        doDelete(id);
    }

}