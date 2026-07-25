package com.aerotech.ced_ops_backend.integration.repository;

import com.aerotech.ced_ops_backend.integration.entity.IntegrationExecutionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationExecutionHistoryRepository extends JpaRepository<IntegrationExecutionHistory, Long> {

    Page<IntegrationExecutionHistory> findByIntegrationIdOrderByCreatedAtDesc(Long integrationId, Pageable pageable);

    Page<IntegrationExecutionHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
