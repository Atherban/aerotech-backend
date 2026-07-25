package com.aerotech.ced_ops_backend.master.line.repository;

import com.aerotech.ced_ops_backend.master.line.entity.Line;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LineRepository extends JpaRepository<Line, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Line> findAllByOrderByDisplayOrderAsc();

}