package com.aerotech.ced_ops_backend.settings.repository;

import com.aerotech.ced_ops_backend.common.enums.SettingCategory;
import com.aerotech.ced_ops_backend.settings.entity.SystemSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

    Optional<SystemSetting> findBySettingKey(String settingKey);

    Page<SystemSetting> findByIsActiveTrue(Pageable pageable);

    Page<SystemSetting> findByCategoryAndIsActiveTrue(SettingCategory category, Pageable pageable);

    Page<SystemSetting> findBySettingKeyContainingIgnoreCaseAndIsActiveTrue(
            String keyword, Pageable pageable);

    List<SystemSetting> findByCategoryAndIsActiveTrue(SettingCategory category);

    boolean existsBySettingKey(String settingKey);

    List<SystemSetting> findAllByIsActiveTrueOrderByCategoryAscSettingKeyAsc();

}
