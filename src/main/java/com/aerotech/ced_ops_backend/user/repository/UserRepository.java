package com.aerotech.ced_ops_backend.user.repository;

import com.aerotech.ced_ops_backend.user.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Query("""
            SELECT u
            FROM User u
            JOIN FETCH u.role
            WHERE u.employeeId = :employeeId
            """)
    Optional<User> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByMobileNumber(String mobileNumber);

    @EntityGraph(attributePaths = "role")
    List<User> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "role")
    Page<User> findAll(Specification<User> spec, Pageable pageable);

}