package com.aerotech.ced_ops_backend.report.engine.repository;

import com.aerotech.ced_ops_backend.report.engine.entity.RecordedValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordedValueRepository extends JpaRepository<RecordedValue, Long> {

    List<RecordedValue> findByRecordedProcess_SessionId(Long sessionId);

    List<RecordedValue> findByRecordedProcessId(Long recordedProcessId);

    void deleteByRecordedProcessId(Long recordedProcessId);

}