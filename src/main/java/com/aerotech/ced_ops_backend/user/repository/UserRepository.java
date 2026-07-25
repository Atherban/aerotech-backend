package com.aerotech.ced_ops_backend.user.repository;

import com.aerotech.ced_ops_backend.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

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

}