package com.aerotech.ced_ops_backend.report.engine.service;

import com.aerotech.ced_ops_backend.common.enums.InputType;
import com.aerotech.ced_ops_backend.common.enums.ReportStatus;
import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.master.line.entity.Line;
import com.aerotech.ced_ops_backend.master.line.repository.LineRepository;
import com.aerotech.ced_ops_backend.master.module.entity.Module;
import com.aerotech.ced_ops_backend.master.module.entity.ModuleType;
import com.aerotech.ced_ops_backend.master.module.entity.Parameter;
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
import com.aerotech.ced_ops_backend.report.engine.dto.RecordProcessRequest;
import com.aerotech.ced_ops_backend.report.engine.dto.RecordProcessResponse;
import com.aerotech.ced_ops_backend.report.engine.dto.RecordedValueRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenericReportEngineServiceTest {

    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private TemplateVersionRepository templateVersionRepository;
    @Mock
    private ProcessRepository processRepository;
    @Mock
    private ProcessParameterRepository processParameterRepository;
    @Mock
    private ReportSessionRepository reportSessionRepository;
    @Mock
    private RecordedProcessRepository recordedProcessRepository;
    @Mock
    private RecordedValueRepository recordedValueRepository;
    @Mock
    private CompletedReportRepository completedReportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private LineRepository lineRepository;

    @InjectMocks
    private GenericReportEngineService service;

    private Module module;
    private ModuleType moduleType;
    private TemplateVersion v2;
    private TemplateVersion v3;
    private Process p1;
    private Process p2;
    private User user;

    private ProcessParameter param1;
    private ProcessParameter param2;

    @BeforeEach
    void setUp() {
        user = User.builder().firstName("A").lastName("B").build();
        user.setId(7L);
        moduleType = ModuleType.builder().name("Inspection").build();
        moduleType.setId(5L);
        module = Module.builder().name("Process Monitoring").prefix("PMR").status(ModuleStatus.ACTIVE).build();
        module.setId(1L);
        module.setModuleType(moduleType);

        v2 = TemplateVersion.builder().module(module).versionNumber(2).status(TemplateVersionStatus.ACTIVE).build();
        v2.setId(20L);
        v3 = TemplateVersion.builder().module(module).versionNumber(3).status(TemplateVersionStatus.ACTIVE).build();
        v3.setId(30L);

        p1 = Process.builder().templateVersion(v2).name("Shot Blasting").displayOrder(1).status(ProcessStatus.ACTIVE).build();
        p1.setId(10L);
        p2 = Process.builder().templateVersion(v2).name("CED Coating").displayOrder(2).status(ProcessStatus.ACTIVE).build();
        p2.setId(11L);

        param1 = makeParameter("Temperature", true);
        param1.setUnit("°C");
        param1.setMinimumValue(new BigDecimal("0"));
        param1.setMaximumValue(new BigDecimal("100"));
        param1.getParameter().setInputType(InputType.NUMBER);
        param2 = makeParameter("Layer", false);
        param2.getParameter().setInputType(InputType.TEXT);
    }

    private ProcessParameter makeParameter(String name, boolean mandatory) {
        Parameter parameter = Parameter.builder().name(name).build();
        parameter.setId((long) name.hashCode());
        return ProcessParameter.builder()
                .parameter(parameter)
                .displayOrder(1)
                .mandatory(mandatory)
                .visible(true)
                .build();
    }

    @Test
    void startFreezesLatestActiveTemplateVersionAndLoadsFirstProcess() {
        lenient().when(templateVersionRepository.findTopByModuleIdAndStatusOrderByVersionNumberDesc(1L, TemplateVersionStatus.ACTIVE))
                .thenReturn(Optional.of(v2));
        when(processRepository.findByTemplateVersionIdAndStatusOrderByDisplayOrderAsc(20L, ProcessStatus.ACTIVE))
                .thenReturn(List.of(p1, p2));
        when(reportSessionRepository.save(any(ReportSession.class)))
                .thenAnswer(invocation -> {
                    ReportSession s = invocation.getArgument(0);
                    s.setId(500L);
                    return s;
                });

        ReportSessionResponse response = service.start(module, user, null, null);

        assertThat(response.getTemplateVersionId()).isEqualTo(20L);
        assertThat(response.getVersionNumber()).isEqualTo(2);
        assertThat(response.getCurrentProcessId()).isEqualTo(10L);
        assertThat(response.getCompletedProcessCount()).isZero();
        assertThat(response.getStatus()).isEqualTo(ReportSessionStatus.IN_PROGRESS);
    }

    @Test
    void startRejectsModuleWithoutActiveVersion() {
        when(templateVersionRepository.findTopByModuleIdAndStatusOrderByVersionNumberDesc(1L, TemplateVersionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.start(module, user, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Module has no ACTIVE template version yet.");
    }

    @Test
    void saveAndNextAdvancesByDisplayOrderAndReturnsNextProcess() {
        ReportSession session = activeSession(500L, v2, p1, 0);

        when(reportSessionRepository.findById(500L)).thenReturn(Optional.of(session));
        when(processParameterRepository.findByProcessIdAndActiveTrueOrderByDisplayOrderAsc(10L))
                .thenReturn(List.of(param1));
        when(recordedProcessRepository.findBySessionIdAndProcessId(500L, 10L))
                .thenReturn(Optional.empty());
        when(recordedProcessRepository.save(any(RecordedProcess.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(processRepository.findByTemplateVersionIdAndStatusOrderByDisplayOrderAsc(20L, ProcessStatus.ACTIVE))
                .thenReturn(List.of(p1, p2));
        when(reportSessionRepository.save(any(ReportSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecordProcessRequest request = RecordProcessRequest.builder()
                .values(List.of(RecordedValueRequest.builder()
                        .processParameterId(param1.getId())
                        .observedValue("10.0")
                        .build()))
                .build();

        RecordProcessResponse response = service.saveAndNext(500L, request);

        assertThat(response.isReportCompleted()).isFalse();
        assertThat(response.getCompletedProcessCount()).isEqualTo(1);
        assertThat(response.getNextProcess().getProcessId()).isEqualTo(11L);
        assertThat(session.getCurrentProcess().getId()).isEqualTo(11L);
    }

    @Test
    void saveAndNextOnLastProcessCompletesAndSubmitsReport() {
        ReportSession session = activeSession(500L, v2, p2, 1);

        when(reportSessionRepository.findById(500L)).thenReturn(Optional.of(session));
        when(processParameterRepository.findByProcessIdAndActiveTrueOrderByDisplayOrderAsc(11L))
                .thenReturn(List.of(param2));
        when(recordedProcessRepository.findBySessionIdAndProcessId(500L, 11L))
                .thenReturn(Optional.empty());
        when(recordedProcessRepository.save(any(RecordedProcess.class)))
                .thenAnswer(invocation -> {
                    RecordedProcess rp = invocation.getArgument(0);
                    rp.setId(900L);
                    return rp;
                });
        when(processRepository.findByTemplateVersionIdAndStatusOrderByDisplayOrderAsc(20L, ProcessStatus.ACTIVE))
                .thenReturn(List.of(p1, p2));
        when(reportSessionRepository.save(any(ReportSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(completedReportRepository.countByModuleId(1L)).thenReturn(0L);
        when(completedReportRepository.save(any(CompletedReport.class)))
                .thenAnswer(invocation -> {
                    CompletedReport report = invocation.getArgument(0);
                    report.setId(800L);
                    return report;
                });

        RecordProcessRequest request = RecordProcessRequest.builder()
                .values(List.of())
                .build();

        RecordProcessResponse response = service.saveAndNext(500L, request);

        assertThat(response.isReportCompleted()).isTrue();
        assertThat(response.getNextProcess()).isNull();
        assertThat(session.getStatus()).isEqualTo(ReportSessionStatus.COMPLETED);
        assertThat(session.getCurrentProcess()).isNull();
        assertThat(response.getReport().getReportNumber()).startsWith("PMR-");
        assertThat(response.getReport().getReportNumber()).endsWith("-00001");
    }

    @Test
    void saveRejectsMissingMandatoryValue() {
        ReportSession session = activeSession(500L, v2, p1, 0);

        when(reportSessionRepository.findById(500L)).thenReturn(Optional.of(session));
        when(processParameterRepository.findByProcessIdAndActiveTrueOrderByDisplayOrderAsc(10L))
                .thenReturn(List.of(param1));

        RecordProcessRequest request = RecordProcessRequest.builder()
                .values(List.of())
                .build();

        assertThatThrownBy(() -> service.saveAndNext(500L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Mandatory parameter");
    }

    @Test
    void saveAndSubmitCreatesCompletedReportWithSubmittedStatus() {
        ReportSession session = activeSession(500L, v2, p2, 1);

        when(reportSessionRepository.findById(500L)).thenReturn(Optional.of(session));
        when(processParameterRepository.findByProcessIdAndActiveTrueOrderByDisplayOrderAsc(11L))
                .thenReturn(List.of(param2));
        when(recordedProcessRepository.findBySessionIdAndProcessId(500L, 11L))
                .thenReturn(Optional.empty());
        when(recordedProcessRepository.save(any(RecordedProcess.class)))
                .thenAnswer(invocation -> {
                    RecordedProcess rp = invocation.getArgument(0);
                    rp.setId(901L);
                    return rp;
                });
        when(reportSessionRepository.save(any(ReportSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(completedReportRepository.countByModuleId(1L)).thenReturn(0L);
        when(completedReportRepository.save(any(CompletedReport.class)))
                .thenAnswer(invocation -> {
                    CompletedReport report = invocation.getArgument(0);
                    report.setId(801L);
                    return report;
                });

        RecordProcessResponse response = service.saveAndSubmit(500L, RecordProcessRequest.builder().values(List.of()).build());

        assertThat(response.isReportCompleted()).isTrue();
        assertThat(response.getReport().getReportNumber()).startsWith("PMR-");
        assertThat(response.getReport().getSessionId()).isEqualTo(500L);

        ArgumentCaptor<RecordedProcess> recordedCaptor = ArgumentCaptor.forClass(RecordedProcess.class);
        verify(recordedProcessRepository).save(recordedCaptor.capture());
        assertThat(recordedCaptor.getValue().getStatus()).isEqualTo(RecordedProcessStatus.COMPLETED);
        assertThat(recordedCaptor.getValue().getProcessOrderSnapshot()).isEqualTo(2);
    }

    @Test
    void sessionNotInProgressCannotBeContinued() {
        ReportSession completed = activeSession(1L, p1, 0);
        completed.setStatus(ReportSessionStatus.COMPLETED);

        when(reportSessionRepository.findById(1L)).thenReturn(Optional.of(completed));

        assertThatThrownBy(() -> service.getCurrentProcess(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Report session is not in progress.");
    }

    @Test
    void startCapturesOptionalShiftAndLine() {
        lenient().when(templateVersionRepository.findTopByModuleIdAndStatusOrderByVersionNumberDesc(1L, TemplateVersionStatus.ACTIVE))
                .thenReturn(Optional.of(v2));
        when(processRepository.findByTemplateVersionIdAndStatusOrderByDisplayOrderAsc(20L, ProcessStatus.ACTIVE))
                .thenReturn(List.of(p1, p2));

        Shift shift = Shift.builder().name("Morning").build();
        shift.setId(1L);
        Line line = Line.builder().name("Line A").build();
        line.setId(2L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(lineRepository.findById(2L)).thenReturn(Optional.of(line));
        when(reportSessionRepository.save(any(ReportSession.class)))
                .thenAnswer(invocation -> {
                    ReportSession s = invocation.getArgument(0);
                    s.setId(501L);
                    return s;
                });

        service.start(module, user, 1L, 2L);

        ArgumentCaptor<ReportSession> captor = ArgumentCaptor.forClass(ReportSession.class);
        verify(reportSessionRepository).save(captor.capture());
        assertThat(captor.getValue().getShiftId()).isEqualTo(1L);
        assertThat(captor.getValue().getShiftName()).isEqualTo("Morning");
        assertThat(captor.getValue().getLineId()).isEqualTo(2L);
        assertThat(captor.getValue().getLineName()).isEqualTo("Line A");
    }

    @Test
    void submitPopulatesImmutableSnapshotsOnCompletedReport() {
        ReportSession session = activeSession(500L, v2, p2, 1);

        when(reportSessionRepository.findById(500L)).thenReturn(Optional.of(session));
        when(processParameterRepository.findByProcessIdAndActiveTrueOrderByDisplayOrderAsc(11L))
                .thenReturn(List.of(param2));
        when(recordedProcessRepository.findBySessionIdAndProcessId(500L, 11L))
                .thenReturn(Optional.empty());
        when(recordedProcessRepository.save(any(RecordedProcess.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(reportSessionRepository.save(any(ReportSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(completedReportRepository.countByModuleId(1L)).thenReturn(5L);
        when(completedReportRepository.save(any(CompletedReport.class)))
                .thenAnswer(invocation -> {
                    CompletedReport report = invocation.getArgument(0);
                    report.setId(802L);
                    return report;
                });

        service.saveAndSubmit(500L, RecordProcessRequest.builder().values(List.of()).build());

        ArgumentCaptor<CompletedReport> captor = ArgumentCaptor.forClass(CompletedReport.class);
        verify(completedReportRepository).save(captor.capture());
        CompletedReport report = captor.getValue();
        assertThat(report.getModuleName()).isEqualTo("Process Monitoring");
        assertThat(report.getModulePrefix()).isEqualTo("PMR");
        assertThat(report.getTemplateVersionNumber()).isEqualTo(2);
        assertThat(report.getModuleTypeId()).isEqualTo(5L);
        assertThat(report.getModuleTypeName()).isEqualTo("Inspection");
        assertThat(report.getShiftId()).isNull();
        assertThat(report.getLineId()).isNull();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.SUBMITTED);
    }

    @Test
    void recordedValuesCarryImmutableSnapshots() {
        ReportSession session = activeSession(500L, v2, p1, 0);

        when(reportSessionRepository.findById(500L)).thenReturn(Optional.of(session));
        when(processParameterRepository.findByProcessIdAndActiveTrueOrderByDisplayOrderAsc(10L))
                .thenReturn(List.of(param1));
        when(recordedProcessRepository.findBySessionIdAndProcessId(500L, 10L))
                .thenReturn(Optional.empty());
        when(recordedProcessRepository.save(any(RecordedProcess.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(processRepository.findByTemplateVersionIdAndStatusOrderByDisplayOrderAsc(20L, ProcessStatus.ACTIVE))
                .thenReturn(List.of(p1, p2));
        when(reportSessionRepository.save(any(ReportSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecordProcessRequest request = RecordProcessRequest.builder()
                .values(List.of(RecordedValueRequest.builder()
                        .processParameterId(param1.getId())
                        .observedValue("42")
                        .build()))
                .build();

        service.saveAndNext(500L, request);

        ArgumentCaptor<RecordedValue> captor = ArgumentCaptor.forClass(RecordedValue.class);
        verify(recordedValueRepository).save(captor.capture());
        RecordedValue value = captor.getValue();
        assertThat(value.getParameterName()).isEqualTo("Temperature");
        assertThat(value.getInputType()).isEqualTo(InputType.NUMBER.name());
        assertThat(value.getUnit()).isEqualTo("°C");
        assertThat(value.getMinimumValue()).isEqualByComparingTo(new BigDecimal("0"));
        assertThat(value.getMaximumValue()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(value.getObservedValue()).isEqualTo("42");
    }

    private ReportSession activeSession(Long id, Process current, int completed) {
        return activeSession(id, v2, current, completed);
    }

    private ReportSession activeSession(Long id, TemplateVersion version, Process current, int completed) {
        ReportSession session = ReportSession.builder()
                .module(module)
                .templateVersion(version)
                .currentProcess(current)
                .completedProcessCount(completed)
                .status(ReportSessionStatus.IN_PROGRESS)
                .createdBy(user)
                .build();
        session.setId(id);
        return session;
    }

}