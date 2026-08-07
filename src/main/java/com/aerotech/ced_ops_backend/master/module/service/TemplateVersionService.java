package com.aerotech.ced_ops_backend.master.module.service;

import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.master.module.dto.CreateTemplateVersionRequest;
import com.aerotech.ced_ops_backend.master.module.dto.ParameterSummaryResponse;
import com.aerotech.ced_ops_backend.master.module.dto.ProcessParameterResponse;
import com.aerotech.ced_ops_backend.master.module.dto.ProcessResponse;
import com.aerotech.ced_ops_backend.master.module.dto.TemplateVersionResponse;
import com.aerotech.ced_ops_backend.master.module.entity.Module;
import com.aerotech.ced_ops_backend.master.module.entity.Process;
import com.aerotech.ced_ops_backend.master.module.entity.ProcessParameter;
import com.aerotech.ced_ops_backend.master.module.entity.TemplateVersion;
import com.aerotech.ced_ops_backend.master.module.enums.ModuleStatus;
import com.aerotech.ced_ops_backend.master.module.enums.TemplateVersionStatus;
import com.aerotech.ced_ops_backend.master.module.repository.ModuleRepository;
import com.aerotech.ced_ops_backend.master.module.repository.ProcessParameterRepository;
import com.aerotech.ced_ops_backend.master.module.repository.ProcessRepository;
import com.aerotech.ced_ops_backend.master.module.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TemplateVersionService {

    private final TemplateVersionRepository templateVersionRepository;
    private final ModuleRepository moduleRepository;
    private final ProcessRepository processRepository;
    private final ProcessParameterRepository processParameterRepository;

    @Transactional(readOnly = true)
    public List<TemplateVersionResponse> getVersions(Long moduleId) {

        getModule(moduleId);

        return templateVersionRepository.findByModuleIdOrderByVersionNumberDesc(moduleId)
                .stream()
                .map(this::toVersionResponse)
                .toList();
    }

    /**
     * Creates the next template version by snapshotting the processes and
     * process parameters of the current latest ACTIVE version into a new DRAFT
     * version. The source ACTIVE version is never modified.
     */
    public TemplateVersionResponse createVersion(Long moduleId, CreateTemplateVersionRequest request) {

        Module module = getModule(moduleId);

        if (module.getStatus() == ModuleStatus.ARCHIVED) {
            throw new BadRequestException("Module is archived and cannot receive new versions.");
        }

        int nextNumber = templateVersionRepository
                .findTopByModuleIdOrderByVersionNumberDesc(moduleId)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        TemplateVersion version = TemplateVersion.builder()
                .module(module)
                .versionNumber(nextNumber)
                .status(TemplateVersionStatus.DRAFT)
                .changeNote(request.getChangeNote())
                .build();

        version = templateVersionRepository.save(version);

        copyLatestActive(moduleId, version);

        log.info("Template version {} created for module {}", nextNumber, module.getName());

        return toVersionResponse(version);
    }

    /**
     * Publishes a DRAFT template version: marks it ACTIVE, supersedes every
     * other ACTIVE version of the module, and activates the module itself.
     */
    public TemplateVersionResponse publish(Long moduleId, Long versionId) {

        getModule(moduleId);

        TemplateVersion version = templateVersionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Template version not found."));

        if (!version.getModule().getId().equals(moduleId)) {
            throw new BadRequestException("Template version does not belong to the given module.");
        }

        if (version.getStatus() != TemplateVersionStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT template versions can be published.");
        }

        List<TemplateVersion> versions = templateVersionRepository.findByModuleIdOrderByVersionNumberDesc(moduleId);
        for (TemplateVersion other : versions) {
            if (other.getStatus() == TemplateVersionStatus.ACTIVE) {
                other.setStatus(TemplateVersionStatus.SUPERSEDED);
                templateVersionRepository.save(other);
            }
        }

        version.setStatus(TemplateVersionStatus.ACTIVE);
        version = templateVersionRepository.save(version);

        Module module = version.getModule();
        if (module.getStatus() == ModuleStatus.DRAFT) {
            module.setStatus(ModuleStatus.ACTIVE);
            moduleRepository.save(module);
        }

        log.info("Template version {} of module {} published", version.getVersionNumber(), module.getName());

        return toVersionResponse(version);
    }

    @Transactional(readOnly = true)
    public List<ProcessResponse> getProcesses(Long versionId) {

        getVersion(versionId);

        return processRepository.findByTemplateVersionIdOrderByDisplayOrderAsc(versionId)
                .stream()
                .map(this::toProcessResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcessParameterResponse> getProcessParameters(Long versionId) {

        List<Process> processes = processRepository.findByTemplateVersionIdOrderByDisplayOrderAsc(versionId);

        List<ProcessParameterResponse> result = new ArrayList<>();
        for (Process process : processes) {
            List<ProcessParameter> bindings = processParameterRepository
                    .findByProcessIdOrderByDisplayOrderAsc(process.getId());
            for (ProcessParameter binding : bindings) {
                result.add(toProcessParameterResponse(binding));
            }
        }

        return result;
    }

    private void copyLatestActive(Long moduleId, TemplateVersion target) {

        TemplateVersion source = templateVersionRepository
                .findTopByModuleIdAndStatusOrderByVersionNumberDesc(moduleId, TemplateVersionStatus.ACTIVE)
                .orElse(null);

        if (source == null) {
            return;
        }

        for (Process sourceProcess : processRepository.findByTemplateVersionIdOrderByDisplayOrderAsc(source.getId())) {
            Process copy = Process.builder()
                    .templateVersion(target)
                    .name(sourceProcess.getName())
                    .description(sourceProcess.getDescription())
                    .displayOrder(sourceProcess.getDisplayOrder())
                    .status(sourceProcess.getStatus())
                    .build();
            copy = processRepository.save(copy);

            for (ProcessParameter binding : processParameterRepository
                    .findByProcessIdOrderByDisplayOrderAsc(sourceProcess.getId())) {
                ProcessParameter bindingCopy = ProcessParameter.builder()
                        .process(copy)
                        .parameter(binding.getParameter())
                        .displayOrder(binding.getDisplayOrder())
                        .mandatory(binding.getMandatory())
                        .visible(binding.getVisible())
                        .defaultValue(binding.getDefaultValue())
                        .unit(binding.getUnit())
                        .minimumValue(binding.getMinimumValue())
                        .maximumValue(binding.getMaximumValue())
                        .active(binding.getActive())
                        .build();
                processParameterRepository.save(bindingCopy);
            }
        }
    }

    private Module getModule(Long moduleId) {

        return moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Module not found."));
    }

    private TemplateVersion getVersion(Long versionId) {

        return templateVersionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Template version not found."));
    }

    private TemplateVersionResponse toVersionResponse(TemplateVersion version) {

        return TemplateVersionResponse.builder()
                .id(version.getId())
                .moduleId(version.getModule().getId())
                .versionNumber(version.getVersionNumber())
                .status(version.getStatus())
                .changeNote(version.getChangeNote())
                .build();
    }

    private ProcessResponse toProcessResponse(Process process) {

        return ProcessResponse.builder()
                .id(process.getId())
                .templateVersionId(process.getTemplateVersion().getId())
                .name(process.getName())
                .description(process.getDescription())
                .displayOrder(process.getDisplayOrder())
                .status(process.getStatus())
                .build();
    }

    private ProcessParameterResponse toProcessParameterResponse(ProcessParameter binding) {

        return ProcessParameterResponse.builder()
                .id(binding.getId())
                .processId(binding.getProcess().getId())
                .parameter(ParameterSummaryResponse.builder()
                        .id(binding.getParameter().getId())
                        .name(binding.getParameter().getName())
                        .build())
                .displayOrder(binding.getDisplayOrder())
                .mandatory(binding.getMandatory())
                .visible(binding.getVisible())
                .defaultValue(binding.getDefaultValue())
                .unit(binding.getUnit())
                .minimumValue(binding.getMinimumValue())
                .maximumValue(binding.getMaximumValue())
                .active(binding.getActive())
                .inputType(binding.getParameter().getInputType())
                .build();
    }

}