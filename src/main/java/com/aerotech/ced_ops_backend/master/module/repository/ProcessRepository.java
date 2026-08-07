package com.aerotech.ced_ops_backend.master.module.repository;

import com.aerotech.ced_ops_backend.master.module.entity.Process;
import com.aerotech.ced_ops_backend.master.module.enums.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProcessRepository extends JpaRepository<Process, Long>, JpaSpecificationExecutor<Process> {

    List<Process> findByTemplateVersionIdOrderByDisplayOrderAsc(Long templateVersionId);

    List<Process> findByTemplateVersionIdAndStatusOrderByDisplayOrderAsc(Long templateVersionId, ProcessStatus status);

    boolean existsByTemplateVersionIdAndNameIgnoreCase(Long templateVersionId, String name);

}