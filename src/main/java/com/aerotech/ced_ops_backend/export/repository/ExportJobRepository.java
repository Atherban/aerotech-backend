package com.aerotech.ced_ops_backend.export.repository;

import com.aerotech.ced_ops_backend.export.entity.ExportJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {

    Page<ExportJob> findByCreatedByOrderByCreatedAtDesc(Long createdBy, Pageable pageable);

}
