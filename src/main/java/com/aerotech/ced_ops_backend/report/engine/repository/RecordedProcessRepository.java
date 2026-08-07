package com.aerotech.ced_ops_backend.report.engine.repository;

import com.aerotech.ced_ops_backend.report.engine.entity.RecordedProcess;
import com.aerotech.ced_ops_backend.report.engine.enums.RecordedProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecordedProcessRepository extends JpaRepository<RecordedProcess, Long> {

    List<RecordedProcess> findBySessionIdOrderByProcessOrderSnapshotAsc(Long sessionId);

    Optional<RecordedProcess> findBySessionIdAndProcessId(Long sessionId, Long processId);

    boolean existsBySessionIdAndProcessId(Long sessionId, Long processId);

    long countBySessionIdAndStatus(Long sessionId, RecordedProcessStatus status);

}