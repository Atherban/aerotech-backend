package com.aerotech.ced_ops_backend.master.module.repository;

import com.aerotech.ced_ops_backend.master.module.entity.TemplateVersion;
import com.aerotech.ced_ops_backend.master.module.enums.TemplateVersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, Long> {

    Optional<TemplateVersion> findTopByModuleIdOrderByVersionNumberDesc(Long moduleId);

    Optional<TemplateVersion> findTopByModuleIdAndStatusOrderByVersionNumberDesc(
            Long moduleId, TemplateVersionStatus status);

    List<TemplateVersion> findByModuleIdOrderByVersionNumberDesc(Long moduleId);

    boolean existsByModuleIdAndVersionNumber(Long moduleId, Integer versionNumber);

}