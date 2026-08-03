package com.aerotech.ced_ops_backend.master.parameter.repository;

import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ParameterMasterRepository
        extends JpaRepository<ParameterMaster, Long>, JpaSpecificationExecutor<ParameterMaster> {

    List<ParameterMaster> findByReportTypeOrderByDisplayOrderAsc(ReportType reportType);

    boolean existsByReportTypeAndParameterNameIgnoreCase(
            ReportType reportType,
            String parameterName
    );

}
