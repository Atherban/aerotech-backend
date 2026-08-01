package com.aerotech.ced_ops_backend.integration.repository;

import com.aerotech.ced_ops_backend.integration.entity.Integration;
import com.aerotech.ced_ops_backend.integration.enums.IntegrationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationRepository extends JpaRepository<Integration, Long> {

    @Query(value = "SELECT i FROM Integration i WHERE " +
           "(:type IS NULL OR i.type = :type) AND " +
           "(:search IS NULL OR LOWER(CAST(i.name AS text)) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "OR LOWER(CAST(i.description AS text)) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')))")
    Page<Integration> findByFilters(@Param("type") IntegrationType type,
                                   @Param("search") String search,
                                   Pageable pageable);

    boolean existsByName(String name);
}
