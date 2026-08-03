package com.aerotech.ced_ops_backend.master.shift.repository;

import com.aerotech.ced_ops_backend.master.shift.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Shift> findAllByOrderByNameAsc();

    List<Shift> findAllByActiveTrueOrderByStartTimeAsc();

}
