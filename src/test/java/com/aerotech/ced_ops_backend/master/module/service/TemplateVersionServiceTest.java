package com.aerotech.ced_ops_backend.master.module.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.master.module.dto.CreateTemplateVersionRequest;
import com.aerotech.ced_ops_backend.master.module.dto.TemplateVersionResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateVersionServiceTest {

    @Mock
    private TemplateVersionRepository templateVersionRepository;
    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private ProcessRepository processRepository;
    @Mock
    private ProcessParameterRepository processParameterRepository;

    @InjectMocks
    private TemplateVersionService templateVersionService;

    private Module module;

    private TemplateVersion version(long id, int number, TemplateVersionStatus status) {
        TemplateVersion v = TemplateVersion.builder()
                .module(module)
                .versionNumber(number)
                .status(status)
                .changeNote("note")
                .build();
        v.setId(id);
        return v;
    }

    @BeforeEach
    void setUp() {
        module = Module.builder()
                .name("Process Monitoring")
                .prefix("PMR")
                .status(ModuleStatus.DRAFT)
                .build();
        module.setId(1L);
    }

    @Test
    void createVersionNumbersSequentiallyFromLatest() {
        TemplateVersion latest = version(2L, 2, TemplateVersionStatus.SUPERSEDED);

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(templateVersionRepository.findTopByModuleIdOrderByVersionNumberDesc(1L))
                .thenReturn(Optional.of(latest));
        when(templateVersionRepository.save(any(TemplateVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TemplateVersionResponse response = templateVersionService.createVersion(
                1L, CreateTemplateVersionRequest.builder().changeNote("v3").build());

        assertThat(response.getVersionNumber()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo(TemplateVersionStatus.DRAFT);
    }

    @Test
    void createVersionFallsBackToVersionOneWhenNoneExists() {
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(templateVersionRepository.findTopByModuleIdOrderByVersionNumberDesc(1L))
                .thenReturn(Optional.empty());
        when(templateVersionRepository.save(any(TemplateVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TemplateVersionResponse response = templateVersionService.createVersion(
                1L, CreateTemplateVersionRequest.builder().changeNote("initial").build());

        assertThat(response.getVersionNumber()).isEqualTo(1);
    }

    @Test
    void publishRejectsNonDraftVersion() {
        TemplateVersion published = version(1L, 1, TemplateVersionStatus.ACTIVE);

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(templateVersionRepository.findById(1L)).thenReturn(Optional.of(published));

        assertThatThrownBy(() -> templateVersionService.publish(1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only DRAFT template versions can be published.");
    }

    @Test
    void publishSupersedesPriorActiveVersionAndActivatesModule() {
        TemplateVersion draft = version(2L, 2, TemplateVersionStatus.DRAFT);
        TemplateVersion active = version(1L, 1, TemplateVersionStatus.ACTIVE);

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(templateVersionRepository.findById(2L)).thenReturn(Optional.of(draft));
        when(templateVersionRepository.findByModuleIdOrderByVersionNumberDesc(1L))
                .thenReturn(List.of(draft, active));
        when(templateVersionRepository.save(any(TemplateVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TemplateVersionResponse response = templateVersionService.publish(1L, 2L);

        assertThat(response.getStatus()).isEqualTo(TemplateVersionStatus.ACTIVE);
        assertThat(active.getStatus()).isEqualTo(TemplateVersionStatus.SUPERSEDED);
        assertThat(module.getStatus()).isEqualTo(ModuleStatus.ACTIVE);
    }

    @Test
    void createVersionSnapshotsProcessesAndBindingsFromLatestActive() {
        TemplateVersion active = version(1L, 1, TemplateVersionStatus.ACTIVE);
        TemplateVersion newDraft = version(2L, 2, TemplateVersionStatus.DRAFT);

        Process sourceProcess = Process.builder()
                .templateVersion(active)
                .name("CED Coating")
                .displayOrder(1)
                .status(ProcessStatus.ACTIVE)
                .build();
        sourceProcess.setId(10L);
        ProcessParameter binding = ProcessParameter.builder()
                .process(sourceProcess)
                .displayOrder(1)
                .active(true)
                .build();
        binding.setId(100L);

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(templateVersionRepository.findTopByModuleIdOrderByVersionNumberDesc(1L))
                .thenReturn(Optional.of(active));
        when(templateVersionRepository.save(any(TemplateVersion.class)))
                .thenReturn(newDraft);
        when(templateVersionRepository.findTopByModuleIdAndStatusOrderByVersionNumberDesc(1L, TemplateVersionStatus.ACTIVE))
                .thenReturn(Optional.of(active));
        when(processRepository.findByTemplateVersionIdOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(sourceProcess));
        when(processRepository.save(any(Process.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(processParameterRepository.findByProcessIdOrderByDisplayOrderAsc(10L))
                .thenReturn(List.of(binding));

        templateVersionService.createVersion(1L, CreateTemplateVersionRequest.builder().changeNote("v2").build());

        verify(processRepository).save(any(Process.class));
        verify(processParameterRepository).save(any(ProcessParameter.class));
        verify(templateVersionRepository, never()).save(active);
    }

}