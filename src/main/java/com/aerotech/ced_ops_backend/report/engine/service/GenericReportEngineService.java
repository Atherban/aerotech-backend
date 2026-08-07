package com.aerotech.ced_ops_backend.report.engine.service;

import com.aerotech.ced_ops_backend.common.enums.ReportStatus;
import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.master.line.entity.Line;
import com.aerotech.ced_ops_backend.master.line.repository.LineRepository;
import com.aerotech.ced_ops_backend.master.module.entity.Module;
import com.aerotech.ced_ops_backend.master.module.entity.Process;
import com.aerotech.ced_ops_backend.master.module.entity.ProcessParameter;
import com.aerotech.ced_ops_backend.master.module.entity.TemplateVersion;
import com.aerotech.ced_ops_backend.master.module.enums.ModuleStatus;
import com.aerotech.ced_ops_backend.master.module.enums.ProcessStatus;
import com.aerotech.ced_ops_backend.master.module.enums.TemplateVersionStatus;
import com.aerotech.ced_ops_backend.master.module.repository.ModuleRepository;
import com.aerotech.ced_ops_backend.master.module.repository.ProcessParameterRepository;
import com.aerotech.ced_ops_backend.master.module.repository.ProcessRepository;
import com.aerotech.ced_ops_backend.master.module.repository.TemplateVersionRepository;
import com.aerotech.ced_ops_backend.master.shift.entity.Shift;
import com.aerotech.ced_ops_backend.master.shift.repository.ShiftRepository;
import com.aerotech.ced_ops_backend.report.engine.dto.CompletedReportResponse;
import com.aerotech.ced_ops_backend.report.engine.dto.ProcessParameterField;
import com.aerotech.ced_ops_backend.report.engine.dto.RecordProcessRequest;
import com.aerotech.ced_ops_backend.report.engine.dto.RecordProcessResponse;
import com.aerotech.ced_ops_backend.report.engine.dto.RecordedProcessItem;
import com.aerotech.ced_ops_backend.report.engine.dto.RecordedValueItem;
import com.aerotech.ced_ops_backend.report.engine.dto.RecordedValueRequest;
import com.aerotech.ced_ops_backend.report.engine.dto.ReportProcessStep;
import com.aerotech.ced_ops_backend.report.engine.dto.ReportSessionResponse;
import com.aerotech.ced_ops_backend.report.engine.entity.CompletedReport;
import com.aerotech.ced_ops_backend.report.engine.entity.RecordedProcess;
import com.aerotech.ced_ops_backend.report.engine.entity.RecordedValue;
import com.aerotech.ced_ops_backend.report.engine.entity.ReportSession;
import com.aerotech.ced_ops_backend.report.engine.enums.RecordedProcessStatus;
import com.aerotech.ced_ops_backend.report.engine.enums.ReportSessionStatus;
import com.aerotech.ced_ops_backend.report.engine.repository.CompletedReportRepository;
import com.aerotech.ced_ops_backend.report.engine.repository.RecordedProcessRepository;
import com.aerotech.ced_ops_backend.report.engine.repository.RecordedValueRepository;
import com.aerotech.ced_ops_backend.report.engine.repository.ReportSessionRepository;
import com.aerotech.ced_ops_backend.user.entity.User;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration-driven generic report engine.
 *
 * <p>Workflow (backend-authoritative):
 * <pre>
 * Start → Create Session (freeze template version) → Load Current Process
 *   → Save Current Process → Return Next Process → repeat
 *   → Final Process → Save &amp; Submit → Completed Report
 * </pre>
 *
 * <p>Navigation uses {@code displayOrder} of the frozen template version only;
 * the frontend simply renders the {@link ReportProcessStep} the backend returns.
 * No report-specific Java code exists — every field and process is derived from
 * the module configuration.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GenericReportEngineService {

    private static final DateTimeFormatter REPORT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ModuleRepository moduleRepository;
    private final TemplateVersionRepository templateVersionRepository;
    private final ProcessRepository processRepository;
    private final ProcessParameterRepository processParameterRepository;
    private final ReportSessionRepository reportSessionRepository;
    private final RecordedProcessRepository recordedProcessRepository;
    private final RecordedValueRepository recordedValueRepository;
    private final CompletedReportRepository completedReportRepository;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final LineRepository lineRepository;

    // ------------------------------------------------------------------
    // Start report
    // ------------------------------------------------------------------

    public ReportSessionResponse start(Module module, User createdBy, Long shiftId, Long lineId) {

        TemplateVersion frozen = templateVersionRepository
                .findTopByModuleIdAndStatusOrderByVersionNumberDesc(module.getId(), TemplateVersionStatus.ACTIVE)
                .orElseThrow(() ->
                        new BadRequestException("Module has no ACTIVE template version yet."));

        List<Process> processes = orderedActiveProcesses(frozen.getId());

        if (processes.isEmpty()) {
            throw new BadRequestException("The ACTIVE template version has no processes configured.");
        }

        Process first = processes.get(0);

        ReportSession session = ReportSession.builder()
                .module(module)
                .templateVersion(frozen)
                .currentProcess(first)
                .startedAt(LocalDateTime.now())
                .completedProcessCount(0)
                .status(ReportSessionStatus.IN_PROGRESS)
                .createdBy(createdBy)
                .build();

        applyShiftAndLine(session, shiftId, lineId);

        session = reportSessionRepository.save(session);

        log.info("Report session {} started for module {} on template version {}",
                session.getId(), module.getName(), frozen.getVersionNumber());

        return toSessionResponse(session);
    }

    // ------------------------------------------------------------------
    // Load current process
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ReportProcessStep getCurrentProcess(Long sessionId) {

        ReportSession session = getActiveSession(sessionId);

        if (session.getCurrentProcess() == null) {
            throw new BadRequestException("This session has no current process (already completed).");
        }

        return toProcessStep(session.getCurrentProcess(), session);
    }

    @Transactional(readOnly = true)
    public List<RecordedProcessItem> getRecordedProcesses(Long sessionId) {

        ReportSession session = getSession(sessionId);

        List<RecordedProcess> recorded = recordedProcessRepository
                .findBySessionIdOrderByProcessOrderSnapshotAsc(sessionId);

        Map<Long, List<RecordedValue>> valuesByRecordedProcess = new HashMap<>();
        for (RecordedValue value : recordedValueRepository.findByRecordedProcess_SessionId(sessionId)) {
            valuesByRecordedProcess
                    .computeIfAbsent(value.getRecordedProcess().getId(), k -> new ArrayList<>())
                    .add(value);
        }

        return recorded.stream()
                .map(rp -> toRecordedProcessItem(rp, valuesByRecordedProcess.getOrDefault(rp.getId(), List.of())))
                .toList();
    }

    // ------------------------------------------------------------------
    // Save current process (Save & Next)
    // ------------------------------------------------------------------

    public RecordProcessResponse saveAndNext(Long sessionId, RecordProcessRequest request) {

        ReportSession session = getActiveSession(sessionId);

        Process current = session.getCurrentProcess();
        if (current == null) {
            throw new BadRequestException("This session has no current process to save.");
        }

        recordProcess(session, current, request);

        List<Process> remaining = nextProcesses(session, current);

        if (remaining.isEmpty()) {
            return completeReport(session);
        }

        Process next = remaining.get(0);
        session.setCurrentProcess(next);
        session = reportSessionRepository.save(session);

        return RecordProcessResponse.builder()
                .sessionId(session.getId())
                .completedProcessCount(session.getCompletedProcessCount())
                .nextProcess(toProcessStep(next, session))
                .reportCompleted(false)
                .build();
    }

    // ------------------------------------------------------------------
    // Save & Submit
    // ------------------------------------------------------------------

    public RecordProcessResponse saveAndSubmit(Long sessionId, RecordProcessRequest request) {

        ReportSession session = getActiveSession(sessionId);

        Process current = session.getCurrentProcess();
        if (current == null) {
            throw new BadRequestException("This session has no current process to save.");
        }

        recordProcess(session, current, request);

        return completeReport(session);
    }

    // ------------------------------------------------------------------
    // Internal workflow
    // ------------------------------------------------------------------

    private void applyShiftAndLine(ReportSession session, Long shiftId, Long lineId) {

        if (shiftId != null) {
            Shift shift = shiftRepository.findById(shiftId)
                    .orElseThrow(() -> new BadRequestException("Shift not found."));
            session.setShiftId(shift.getId());
            session.setShiftName(shift.getName());
        }

        if (lineId != null) {
            Line line = lineRepository.findById(lineId)
                    .orElseThrow(() -> new BadRequestException("Line not found."));
            session.setLineId(line.getId());
            session.setLineName(line.getName());
        }
    }

    private void recordProcess(ReportSession session, Process process, RecordProcessRequest request) {

        List<ProcessParameter> parameters = processParameterRepository
                .findByProcessIdAndActiveTrueOrderByDisplayOrderAsc(process.getId());

        Map<Long, String> submitted = new HashMap<>();
        for (RecordedValueRequest value : request.getValues()) {
            if (submitted.put(value.getProcessParameterId(), value.getObservedValue()) != null) {
                throw new BadRequestException("Duplicate value submitted for process parameter " + value.getProcessParameterId() + ".");
            }
        }

        validateMandatory(parameters, submitted);

        RecordedProcess recorded = recordedProcessRepository
                .findBySessionIdAndProcessId(session.getId(), process.getId())
                .orElseGet(() -> RecordedProcess.builder()
                        .session(session)
                        .process(process)
                        .processOrderSnapshot(process.getDisplayOrder())
                        .status(RecordedProcessStatus.IN_PROGRESS)
                        .build());

        recorded.setProcessOrderSnapshot(process.getDisplayOrder());
        recorded.setStatus(RecordedProcessStatus.COMPLETED);
        recorded.setCompletedAt(LocalDateTime.now());

        recorded = recordedProcessRepository.save(recorded);

        recordedValueRepository.deleteByRecordedProcessId(recorded.getId());

        for (ProcessParameter parameter : parameters) {
            String observed = submitted.get(parameter.getId());
            if (observed == null && Boolean.TRUE.equals(parameter.getVisible())
                    && Boolean.TRUE.equals(parameter.getMandatory())) {
                continue;
            }

            RecordedValue value = RecordedValue.builder()
                    .recordedProcess(recorded)
                    .processParameter(parameter)
                    .parameter(parameter.getParameter())
                    .observedValue(observed)
                    .parameterName(parameter.getParameter().getName())
                    .unit(parameter.getUnit())
                    .inputType(parameter.getParameter().getInputType() != null
                            ? parameter.getParameter().getInputType().name()
                            : null)
                    .minimumValue(parameter.getMinimumValue())
                    .maximumValue(parameter.getMaximumValue())
                    .build();

            recordedValueRepository.save(value);
        }

        session.setCompletedProcessCount(session.getCompletedProcessCount() + 1);
    }

    private void validateMandatory(List<ProcessParameter> parameters, Map<Long, String> submitted) {

        for (ProcessParameter parameter : parameters) {
            if (Boolean.TRUE.equals(parameter.getMandatory())
                    && Boolean.TRUE.equals(parameter.getVisible())) {
                String value = submitted.get(parameter.getId());
                if (value == null || value.isBlank()) {
                    throw new BadRequestException(
                            "Mandatory parameter '" + parameter.getParameter().getName() + "' has no value.");
                }
            }
        }
    }

    private RecordProcessResponse completeReport(ReportSession session) {

        session.setCurrentProcess(null);
        session.setSubmittedAt(LocalDateTime.now());
        session.setStatus(ReportSessionStatus.COMPLETED);
        session = reportSessionRepository.save(session);

        CompletedReport report = CompletedReport.builder()
                .reportNumber(nextReportNumber(session.getModule()))
                .module(session.getModule())
                .templateVersion(session.getTemplateVersion())
                .startedAt(session.getStartedAt())
                .submittedAt(session.getSubmittedAt())
                .status(ReportStatus.SUBMITTED)
                .createdBy(session.getCreatedBy())
                .sessionId(session.getId())
                .moduleName(session.getModule().getName())
                .modulePrefix(session.getModule().getPrefix())
                .templateVersionNumber(session.getTemplateVersion().getVersionNumber())
                .moduleTypeId(session.getModule().getModuleType() != null
                        ? session.getModule().getModuleType().getId()
                        : null)
                .moduleTypeName(session.getModule().getModuleType() != null
                        ? session.getModule().getModuleType().getName()
                        : session.getModule().getName())
                .shiftId(session.getShiftId())
                .shiftName(session.getShiftName())
                .lineId(session.getLineId())
                .lineName(session.getLineName())
                .build();

        report = completedReportRepository.save(report);

        log.info("Report {} submitted from session {}", report.getReportNumber(), session.getId());

        return RecordProcessResponse.builder()
                .sessionId(session.getId())
                .completedProcessCount(session.getCompletedProcessCount())
                .nextProcess(null)
                .reportCompleted(true)
                .report(toReportResponse(report))
                .build();
    }

    private String nextReportNumber(Module module) {

        long sequence = completedReportRepository.countByModuleId(module.getId()) + 1;

        return "%s-%s-%05d".formatted(
                module.getPrefix(),
                LocalDate.now().format(REPORT_DATE_FORMATTER),
                sequence
        );
    }

    // ------------------------------------------------------------------
    // Navigation (displayOrder only)
    // ------------------------------------------------------------------

    private List<Process> orderedActiveProcesses(Long templateVersionId) {

        return processRepository
                .findByTemplateVersionIdAndStatusOrderByDisplayOrderAsc(templateVersionId, ProcessStatus.ACTIVE);
    }

    /**
     * Processes of the frozen template that come after the just-completed one,
     * in displayOrder. Ordering is never inferred — only displayOrder is used.
     */
    private List<Process> nextProcesses(ReportSession session, Process current) {

        return orderedActiveProcesses(session.getTemplateVersion().getId())
                .stream()
                .filter(p -> p.getDisplayOrder() > current.getDisplayOrder())
                .toList();
    }

    // ------------------------------------------------------------------
    // Session / step builders
    // ------------------------------------------------------------------

    private ReportSession getSession(Long sessionId) {

        return reportSessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Report session not found."));
    }

    private ReportSession getActiveSession(Long sessionId) {

        ReportSession session = getSession(sessionId);

        if (session.getStatus() != ReportSessionStatus.IN_PROGRESS) {
            throw new BadRequestException("Report session is not in progress.");
        }

        return session;
    }

    private ReportSessionResponse toSessionResponse(ReportSession session) {

        return ReportSessionResponse.builder()
                .id(session.getId())
                .moduleId(session.getModule().getId())
                .moduleName(session.getModule().getName())
                .templateVersionId(session.getTemplateVersion().getId())
                .versionNumber(session.getTemplateVersion().getVersionNumber())
                .currentProcessId(session.getCurrentProcess() != null ? session.getCurrentProcess().getId() : null)
                .startedAt(session.getStartedAt())
                .completedProcessCount(session.getCompletedProcessCount())
                .status(session.getStatus())
                .build();
    }

    private ReportProcessStep toProcessStep(Process process, ReportSession session) {

        boolean last = nextProcesses(session, process).isEmpty();

        List<ProcessParameterField> fields = processParameterRepository
                .findByProcessIdAndActiveTrueOrderByDisplayOrderAsc(process.getId())
                .stream()
                .map(this::toField)
                .toList();

        return ReportProcessStep.builder()
                .processId(process.getId())
                .name(process.getName())
                .description(process.getDescription())
                .displayOrder(process.getDisplayOrder())
                .lastProcess(last)
                .fields(fields)
                .build();
    }

    private ProcessParameterField toField(ProcessParameter parameter) {

        return ProcessParameterField.builder()
                .processParameterId(parameter.getId())
                .parameterId(parameter.getParameter().getId())
                .parameterName(parameter.getParameter().getName())
                .inputType(parameter.getParameter().getInputType())
                .mandatory(parameter.getMandatory())
                .unit(parameter.getUnit())
                .minimumValue(parameter.getMinimumValue())
                .maximumValue(parameter.getMaximumValue())
                .defaultValue(parameter.getDefaultValue())
                .build();
    }

    private RecordedProcessItem toRecordedProcessItem(RecordedProcess recorded, List<RecordedValue> values) {

        return RecordedProcessItem.builder()
                .id(recorded.getId())
                .processId(recorded.getProcess().getId())
                .processName(recorded.getProcess().getName())
                .processOrderSnapshot(recorded.getProcessOrderSnapshot())
                .values(values.stream()
                        .map(this::toRecordedValueItem)
                        .toList())
                .build();
    }

    private RecordedValueItem toRecordedValueItem(RecordedValue value) {

        return RecordedValueItem.builder()
                .id(value.getId())
                .processParameterId(value.getProcessParameter().getId())
                .parameterId(value.getParameter().getId())
                .parameterName(value.getParameter().getName())
                .inputType(value.getParameter().getInputType())
                .observedValue(value.getObservedValue())
                .build();
    }

    private CompletedReportResponse toReportResponse(CompletedReport report) {

        return CompletedReportResponse.builder()
                .id(report.getId())
                .reportNumber(report.getReportNumber())
                .moduleName(report.getModule().getName())
                .versionNumber(report.getTemplateVersion().getVersionNumber())
                .prefix(report.getModule().getPrefix())
                .startedAt(report.getStartedAt())
                .submittedAt(report.getSubmittedAt())
                .status(report.getStatus())
                .sessionId(report.getSessionId())
                .build();
    }

    // ------------------------------------------------------------------
    // Lookups used by the controller
    // ------------------------------------------------------------------

    public Module getModuleOrThrow(Long moduleId) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Module not found."));

        if (module.getStatus() != ModuleStatus.ACTIVE) {
            throw new BadRequestException("Module is not active.");
        }

        return module;
    }

    public User currentUser() {

        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();

        return userRepository.findByEmployeeId(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));
    }

    public CompletedReportResponse getCompletedReport(Long reportId) {

        CompletedReport report = completedReportRepository.findById(reportId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Report not found."));

        return toReportResponse(report);
    }

    @Transactional(readOnly = true)
    public List<CompletedReportResponse> getMyReports(Long userId) {

        return completedReportRepository.findByCreatedByIdOrderBySubmittedAtDesc(userId)
                .stream()
                .map(this::toReportResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportSessionResponse> getMySessions(Long userId) {

        return reportSessionRepository.findByCreatedByIdAndStatusOrderByCreatedAtDesc(userId, ReportSessionStatus.IN_PROGRESS)
                .stream()
                .map(this::toSessionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportSessionResponse getSessionResponse(Long sessionId) {

        return toSessionResponse(getSession(sessionId));
    }

}