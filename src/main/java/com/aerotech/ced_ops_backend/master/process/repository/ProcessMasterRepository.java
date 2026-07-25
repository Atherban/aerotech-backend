package com.aerotech.ced_ops_backend.master.process.repository;

import com.aerotech.ced_ops_backend.master.process.entity.ProcessMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessMasterRepository extends JpaRepository<ProcessMaster, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<ProcessMaster> findAllByOrderByDisplayOrderAsc();

}