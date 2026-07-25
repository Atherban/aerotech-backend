package com.aerotech.ced_ops_backend.master.parameter.repository;

import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParameterMasterRepository extends JpaRepository<ParameterMaster,Long> {

    List<ParameterMaster> findByProcessIdOrderByDisplayOrderAsc(Long processId);

    boolean existsByProcessIdAndParameterNameIgnoreCase(
            Long processId,
            String parameterName
    );

}