package com.aerotech.ced_ops_backend.report.chemical.service;

import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.common.service.ValidationService;
import com.aerotech.ced_ops_backend.common.util.ReportNumberGenerator;
import com.aerotech.ced_ops_backend.master.line.repository.LineRepository;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.master.parameter.repository.ParameterMasterRepository;
import com.aerotech.ced_ops_backend.master.shift.service.ShiftService;
import com.aerotech.ced_ops_backend.notification.service.NotificationChannel;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.ApproveChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.CreateChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.request.SubmitChemicalConsumptionRequest;
import com.aerotech.ced_ops_backend.report.chemical.dto.response.ChemicalConsumptionResponse;
import com.aerotech.ced_ops_backend.report.chemical.entity.ChemicalConsumptionEntry;
import com.aerotech.ced_ops_backend.report.chemical.entity.ChemicalConsumptionReport;
import com.aerotech.ced_ops_backend.report.chemical.mapper.ChemicalConsumptionMapper;
import com.aerotech.ced_ops_backend.report.chemical.repository.ChemicalConsumptionEntryRepository;
import com.aerotech.ced_ops_backend.report.chemical.repository.ChemicalConsumptionReportRepository;
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
public class ChemicalConsumptionService
        extends AbstractReportService<ChemicalConsumptionReport, ChemicalConsumptionEntry, ChemicalConsumptionResponse> {

    private final ChemicalConsumptionReportRepository reportRepository;

    private final ChemicalConsumptionEntryRepository entryRepository;

    private final ChemicalConsumptionMapper mapper;

    public ChemicalConsumptionService(
            ReportNumberGenerator reportNumberGenerator,
            ValidationService validationService,
            ShiftService shiftService,
            LineRepository lineRepository,
            ParameterMasterRepository parameterRepository,
            UserRepository userRepository,
            NotificationChannel notificationChannel,
            ChemicalConsumptionReportRepository reportRepository,
            ChemicalConsumptionEntryRepository entryRepository,
            ChemicalConsumptionMapper mapper
    ) {
        super(reportNumberGenerator, validationService, shiftService, lineRepository, parameterRepository, userRepository, notificationChannel);
        this.reportRepository = reportRepository;
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Override
    protected ReportType reportType() {
        return ReportType.CHEMICAL_CONSUMPTION;
    }

    @Override
    protected ChemicalConsumptionReport getReportOrThrow(Long id) {
        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException(
                                reportLabel() + " report not found."));
    }

    @Override
    protected List<ChemicalConsumptionReport> findReportsWithDetails() {
        return reportRepository.findAllWithDetails();
    }

    @Override
    protected ChemicalConsumptionReport saveReport(ChemicalConsumptionReport report) {
        return reportRepository.save(report);
    }

    @Override
    protected void deleteReport(ChemicalConsumptionReport report) {
        reportRepository.delete(report);
    }

    @Override
    protected long reportCount() {
        return reportRepository.count();
    }

    @Override
    protected List<ChemicalConsumptionEntry> entriesOf(ChemicalConsumptionReport report) {
        return entryRepository.findByReport(report);
    }

    @Override
    protected Map<Long, List<ChemicalConsumptionEntry>> entriesGroupedByReport(
            List<Long> reportIds
    ) {
        return entryRepository.findByReportIdIn(reportIds)
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getReport().getId()));
    }

    @Override
    protected List<ChemicalConsumptionEntry> saveEntries(List<ChemicalConsumptionEntry> entries) {
        return entryRepository.saveAll(entries);
    }

    @Override
    protected void deleteEntriesByReportId(Long reportId) {
        entryRepository.deleteByReportId(reportId);
    }

    @Override
    protected ChemicalConsumptionResponse toResponse(
            ChemicalConsumptionReport report,
            List<ChemicalConsumptionEntry> entries
    ) {
        return mapper.toResponse(report, entries);
    }

    @Override
    protected ChemicalConsumptionEntry newEntry(
            ChemicalConsumptionReport report,
            ParameterMaster parameter,
            String observedValue,
            InspectionResult result,
            String remark
    ) {
        return ChemicalConsumptionEntry.builder()
                .report(report)
                .parameter(parameter)
                .observedValue(observedValue)
                .inspectionResult(result)
                .remark(remark)
                .build();
    }

    public ChemicalConsumptionResponse create(CreateChemicalConsumptionRequest request) {

        ChemicalConsumptionReport report = new ChemicalConsumptionReport();

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

    public List<ChemicalConsumptionResponse> getAll() {
        return doGetAll();
    }

    public PageResponse<ChemicalConsumptionResponse> search(ReportFilterRequest filter) {
        return doSearch(filter);
    }

    public ChemicalConsumptionResponse getById(Long id) {
        return doGetById(id);
    }

    public ChemicalConsumptionResponse submit(Long id, SubmitChemicalConsumptionRequest request) {
        return doSubmit(id, request::getRemarks);
    }

    public ChemicalConsumptionResponse approve(Long id, ApproveChemicalConsumptionRequest request) {
        return doApprove(id, request::getRemarks);
    }

    public ChemicalConsumptionResponse reject(Long id, ApproveChemicalConsumptionRequest request) {
        return doReject(id, request::getRemarks);
    }

    public void delete(Long id) {
        doDelete(id);
    }

}