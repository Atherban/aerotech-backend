package com.aerotech.ced_ops_backend.report.support;

import com.aerotech.ced_ops_backend.common.entity.BaseReport;
import com.aerotech.ced_ops_backend.common.enums.NotificationType;
import com.aerotech.ced_ops_backend.common.enums.ReportStatus;
import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.pagination.PageRequest;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.common.service.ValidationService;
import com.aerotech.ced_ops_backend.common.util.ReportNumberGenerator;
import com.aerotech.ced_ops_backend.master.line.entity.Line;
import com.aerotech.ced_ops_backend.master.line.repository.LineRepository;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import com.aerotech.ced_ops_backend.master.parameter.repository.ParameterMasterRepository;
import com.aerotech.ced_ops_backend.master.shift.entity.Shift;
import com.aerotech.ced_ops_backend.master.shift.service.ShiftService;
import com.aerotech.ced_ops_backend.notification.service.NotificationChannel;
import com.aerotech.ced_ops_backend.user.entity.User;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Reusable report engine.
 *
 * <p>Encapsulates the common report workflow shared by every report module:
 * listing, single read, submission, approval/rejection and deletion, together with
 * the shared collaborators (current user, shift/line/parameter resolution, report
 * number generation and validation). Report-specific behaviour is supplied by thin
 * subclasses through the abstract hooks - there is no generic report builder here.
 *
 * <p>Concrete subclasses remain {@code @Service} beans with their own
 * {@code @Transactional} semantics; they only wire in their individual Spring Data
 * repositories and their module response mapper.
 *
 * @param <R>  the concrete report entity (extends {@link BaseReport})
 * @param <E>  the concrete report entry entity
 * @param <RO> the report response DTO
 */
public abstract class AbstractReportService<R extends BaseReport, E, RO> {

    protected final ReportNumberGenerator reportNumberGenerator;
    protected final ValidationService validationService;
    protected final ShiftService shiftService;
    protected final LineRepository lineRepository;
    protected final ParameterMasterRepository parameterRepository;
    protected final UserRepository userRepository;
    protected final NotificationChannel notificationChannel;

    protected AbstractReportService(
            ReportNumberGenerator reportNumberGenerator,
            ValidationService validationService,
            ShiftService shiftService,
            LineRepository lineRepository,
            ParameterMasterRepository parameterRepository,
            UserRepository userRepository,
            NotificationChannel notificationChannel
    ) {
        this.reportNumberGenerator = reportNumberGenerator;
        this.validationService = validationService;
        this.shiftService = shiftService;
        this.lineRepository = lineRepository;
        this.parameterRepository = parameterRepository;
        this.userRepository = userRepository;
        this.notificationChannel = notificationChannel;
    }

    // ------------------------------------------------------------------
    // Report-type configuration
    // ------------------------------------------------------------------

    protected abstract ReportType reportType();

    protected String reportLabel() {
        return ReportTypeMetadata.of(reportType()).getLabel();
    }

    // ------------------------------------------------------------------
    // Persistence hooks (each implemented in one line using the module's repo)
    // ------------------------------------------------------------------

    protected abstract R getReportOrThrow(Long id);

    protected abstract List<R> findReportsWithDetails();

    protected abstract R saveReport(R report);

    protected abstract void deleteReport(R report);

    protected abstract long reportCount();

    protected abstract List<E> entriesOf(R report);

    protected abstract Map<Long, List<E>> entriesGroupedByReport(List<Long> reportIds);

    protected abstract List<E> saveEntries(List<E> entries);

    protected abstract void deleteEntriesByReportId(Long reportId);

    // ------------------------------------------------------------------
    // Output / entry hooks
    // ------------------------------------------------------------------

    protected abstract RO toResponse(R report, List<E> entries);

    protected abstract E newEntry(
            R report,
            ParameterMaster parameter,
            String observedValue,
            com.aerotech.ced_ops_backend.common.enums.InspectionResult result,
            String remark
    );

    // ------------------------------------------------------------------
    // Workflow
    // ------------------------------------------------------------------

    protected RO doCreateReport(
            R report,
            LocalDate reportDate,
            Long shiftId,
            Long lineId,
            String remarks,
            Function<R, List<E>> entriesFactory
    ) {
        report.setReportNumber(nextNumber());
        report.setReportType(reportType());
        report.setReportDate(reportDate);
        report.setShift(resolveShift(shiftId));
        report.setLine(getLine(lineId));
        report.setStatus(ReportStatus.DRAFT);
        report.setCreatedBy(currentUser());
        report.setRemarks(remarks);

        report = saveReport(report);

        List<E> entries = entriesFactory.apply(report);
        entries = saveEntries(entries);

        notifyReportCreated(report);

        return toResponse(report, entries);
    }

    protected List<RO> doGetAll() {

        List<R> reports = findReportsWithDetails();

        List<Long> reportIds = reports.stream()
                .map(BaseReport::getId)
                .toList();

        if (reportIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<E>> entriesByReportId = entriesGroupedByReport(reportIds);

        return reports.stream()
                .map(report -> toResponse(
                        report,
                        entriesByReportId.getOrDefault(report.getId(), List.of())
                ))
                .toList();

    }

    /**
     * Paginated + filtered report listing. Additive to {@link #doGetAll()}: the
     * existing full-list read is untouched. Filters, sorts and pages the report
     * rows first, then batch-loads entries only for the current page.
     */
    protected PageResponse<RO> doSearch(ReportFilterRequest filter) {

        List<R> reports = findReportsWithDetails();

        List<R> filtered = reports.stream()
                .filter(report -> matches(report, filter))
                .sorted(comparatorFor(filter))
                .toList();

        long totalElements = filtered.size();
        int page = filter.pageOrDefault();
        int size = filter.sizeOrDefault();
        int from = page * size;
        int to = Math.min(from + size, filtered.size());

        List<R> pageReports = from >= filtered.size()
                ? List.of()
                : filtered.subList(from, to);

        List<Long> pageIds = pageReports.stream()
                .map(BaseReport::getId)
                .toList();

        Map<Long, List<E>> entriesByReportId = pageIds.isEmpty()
                ? Map.of()
                : entriesGroupedByReport(pageIds);

        List<RO> content = pageReports.stream()
                .map(report -> toResponse(
                        report,
                        entriesByReportId.getOrDefault(report.getId(), List.of())
                ))
                .toList();

        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        return PageResponse.<RO>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1 || totalPages == 0)
                .build();
    }

    protected RO doGetById(Long id) {

        R report = getReportOrThrow(id);

        return toResponse(report, entriesOf(report));

    }

    protected RO doSubmit(Long id, Supplier<String> remarks) {

        R report = getReportOrThrow(id);

        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be submitted.");
        }

        report.setStatus(ReportStatus.SUBMITTED);

        mergeRemarks(report, remarks);

        report = saveReport(report);

        notifyReportSubmitted(report);

        return toResponse(report, entriesOf(report));
    }

    protected RO doApprove(Long id, Supplier<String> remarks) {
        return completeApproval(id, remarks, ReportStatus.APPROVED);
    }

    protected RO doReject(Long id, Supplier<String> remarks) {
        return completeApproval(id, remarks, ReportStatus.REJECTED);
    }

    private RO completeApproval(Long id, Supplier<String> remarks, ReportStatus status) {

        R report = getReportOrThrow(id);

        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted reports can be approved or rejected.");
        }

        report.setStatus(status);
        report.setApprovedBy(currentUser());
        report.setApprovedAt(LocalDateTime.now());

        mergeRemarks(report, remarks);

        report = saveReport(report);

        notifyReportDecision(report, status);

        return toResponse(report, entriesOf(report));
    }

    protected void doDelete(Long id) {

        R report = getReportOrThrow(id);

        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be deleted.");
        }

        deleteEntriesByReportId(id);
        deleteReport(report);
    }

    // ------------------------------------------------------------------
    // Shared helpers
    // ------------------------------------------------------------------

    private void mergeRemarks(R report, Supplier<String> remarks) {

        if (remarks == null) {
            return;
        }

        String value = remarks.get();

        if (value != null) {
            report.setRemarks(value);
        }
    }

    private void notifyReportCreated(R report) {
        Long recipientId = report.getCreatedBy() != null ? report.getCreatedBy().getId() : null;
        if (recipientId == null) {
            return;
        }
        notificationChannel.notify(
                NotificationType.REPORT_CREATED,
                recipientId,
                "Report Created",
                reportLabel() + " report " + report.getReportNumber() + " has been created.",
                reportType().name(),
                String.valueOf(report.getId()),
                null
        );
    }

    private void notifyReportSubmitted(R report) {
        Long recipientId = report.getCreatedBy() != null ? report.getCreatedBy().getId() : null;
        if (recipientId == null) {
            return;
        }
        notificationChannel.notify(
                NotificationType.REPORT_SUBMITTED,
                recipientId,
                "Report Submitted",
                reportLabel() + " report " + report.getReportNumber() + " has been submitted for approval.",
                reportType().name(),
                String.valueOf(report.getId()),
                null
        );
    }

    private void notifyReportDecision(R report, ReportStatus status) {
        Long recipientId = report.getCreatedBy() != null ? report.getCreatedBy().getId() : null;
        if (recipientId == null) {
            return;
        }
        boolean approved = status == ReportStatus.APPROVED;
        notificationChannel.notify(
                approved ? NotificationType.REPORT_APPROVED : NotificationType.REPORT_REJECTED,
                recipientId,
                approved ? "Report Approved" : "Report Rejected",
                reportLabel() + " report " + report.getReportNumber()
                        + (approved ? " has been approved." : " has been rejected."),
                reportType().name(),
                String.valueOf(report.getId()),
                null
        );
    }

    protected E buildEntry(
            R report,
            Long parameterId,
            String observedValue,
            String remark
    ) {
        ParameterMaster parameter = getParameter(parameterId);
        var result = validationService.validate(parameter, observedValue);
        return newEntry(report, parameter, observedValue, result, remark);
    }

    protected User currentUser() {

        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();

        return userRepository.findByEmployeeId(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));
    }

    protected Shift resolveShift(Long id) {

        if (id == null) {
            return shiftService.findShiftFor(LocalTime.now());
        }

        return shiftService.getShiftEntity(id);
    }

    protected Line getLine(Long id) {

        return lineRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Line not found."));
    }

    protected ParameterMaster getParameter(Long id) {

        return parameterRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Parameter not found."));
    }

    // ------------------------------------------------------------------
    // Shared report-listing filters (used by doSearch)
    // ------------------------------------------------------------------

    private boolean matches(R report, ReportFilterRequest filter) {

        if (filter.getReportNumber() != null && !filter.getReportNumber().isBlank()
                && !report.getReportNumber().toLowerCase()
                .contains(filter.getReportNumber().trim().toLowerCase())) {
            return false;
        }

        if (filter.getStatus() != null && report.getStatus() != filter.getStatus()) {
            return false;
        }

        if (filter.getShiftId() != null && (report.getShift() == null
                || !filter.getShiftId().equals(report.getShift().getId()))) {
            return false;
        }

        if (filter.getLineId() != null && (report.getLine() == null
                || !filter.getLineId().equals(report.getLine().getId()))) {
            return false;
        }

        if (filter.getDateFrom() != null && report.getReportDate().isBefore(filter.getDateFrom())) {
            return false;
        }

        if (filter.getDateTo() != null && report.getReportDate().isAfter(filter.getDateTo())) {
            return false;
        }

        if (filter.getApproved() != null) {
            boolean isApproved = report.getStatus() == ReportStatus.APPROVED;
            if (filter.getApproved() != isApproved) {
                return false;
            }
        }

        if (PageRequest.isPresent(filter.getKeyword())) {
            String keyword = filter.getKeyword().trim().toLowerCase();
            boolean hit = report.getReportNumber().toLowerCase().contains(keyword)
                    || report.getRemarks() != null && report.getRemarks().toLowerCase().contains(keyword)
                    || report.getCreatedBy() != null
                    && (report.getCreatedBy().getFirstName() + " " + report.getCreatedBy().getLastName())
                    .trim().toLowerCase().contains(keyword);
            if (!hit) {
                return false;
            }
        }

        return true;
    }

    private Comparator<R> comparatorFor(ReportFilterRequest filter) {

        String sortBy = filter.getSortBy();
        Comparator<R> comparator;

        if ("reportDate".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(BaseReport::getReportDate);
        } else if ("reportNumber".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(BaseReport::getReportNumber);
        } else if ("createdAt".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(BaseReport::getCreatedAt);
        } else if ("updatedAt".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(BaseReport::getUpdatedAt);
        } else if ("status".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(BaseReport::getStatus);
        } else {
            comparator = Comparator.comparing(BaseReport::getId);
        }

        boolean desc = !"ASC".equalsIgnoreCase(filter.getSortDirection());
        return desc ? comparator.reversed() : comparator;
    }

    protected String nextNumber() {

        long sequence = reportCount() + 1;

        return reportNumberGenerator.generate(reportType(), sequence);
    }

}