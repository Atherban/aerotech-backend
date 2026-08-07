package com.aerotech.ced_ops_backend.master.module.repository;

import com.aerotech.ced_ops_backend.master.module.entity.ModuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ModuleTypeRepository extends JpaRepository<ModuleType, Long>, JpaSpecificationExecutor<ModuleType> {

    boolean existsByNameIgnoreCase(String name);

    Optional<ModuleType> findByNameIgnoreCase(String name);

}