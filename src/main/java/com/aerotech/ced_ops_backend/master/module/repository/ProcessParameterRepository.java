package com.aerotech.ced_ops_backend.master.module.repository;

import com.aerotech.ced_ops_backend.master.module.entity.ProcessParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessParameterRepository extends JpaRepository<ProcessParameter, Long> {

    List<ProcessParameter> findByProcessIdOrderByDisplayOrderAsc(Long processId);

    List<ProcessParameter> findByProcessIdAndActiveTrueOrderByDisplayOrderAsc(Long processId);

    boolean existsByProcessIdAndParameterId(Long processId, Long parameterId);

}