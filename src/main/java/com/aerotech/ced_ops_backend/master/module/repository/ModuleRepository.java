package com.aerotech.ced_ops_backend.master.module.repository;

import com.aerotech.ced_ops_backend.master.module.entity.Module;
import com.aerotech.ced_ops_backend.master.module.enums.ModuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long>, JpaSpecificationExecutor<Module> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByPrefixIgnoreCase(String prefix);

    Optional<Module> findByNameIgnoreCase(String name);

    List<Module> findByStatusOrderByNameAsc(ModuleStatus status);

}