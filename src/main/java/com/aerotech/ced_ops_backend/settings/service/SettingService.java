package com.aerotech.ced_ops_backend.settings.service;

import com.aerotech.ced_ops_backend.common.enums.SettingCategory;
import com.aerotech.ced_ops_backend.common.enums.SettingDataType;
import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.settings.dto.request.BulkUpdateSettingsRequest;
import com.aerotech.ced_ops_backend.settings.dto.request.CreateSettingRequest;
import com.aerotech.ced_ops_backend.settings.dto.request.UpdateSettingRequest;
import com.aerotech.ced_ops_backend.settings.dto.response.SystemSettingResponse;
import com.aerotech.ced_ops_backend.settings.entity.SystemSetting;
import com.aerotech.ced_ops_backend.settings.mapper.SystemSettingMapper;
import com.aerotech.ced_ops_backend.settings.repository.SystemSettingRepository;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingService {

    private final SystemSettingRepository repository;
    private final SystemSettingMapper mapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "settings", key = "#key")
    public SystemSettingResponse getByKey(String key) {
        SystemSetting setting = repository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Setting not found with key: " + key));
        return mapper.toResponse(setting);
    }

    @Transactional(readOnly = true)
    public List<SystemSettingResponse> getAll() {
        List<SystemSetting> settings = repository.findAllByIsActiveTrueOrderByCategoryAscSettingKeyAsc();
        return mapper.toResponseList(settings);
    }

    @Transactional
    @CacheEvict(value = "settings", key = "#request.settingKey")
    public SystemSettingResponse create(CreateSettingRequest request) {
        if (repository.existsBySettingKey(request.getSettingKey())) {
            throw new BadRequestException("Setting already exists with key: " + request.getSettingKey());
        }

        SettingCategory category = parseCategory(request.getCategory());
        SettingDataType dataType = parseDataType(request.getDataType());

        validateValue(request.getSettingValue(), dataType);

        LocalDateTime now = LocalDateTime.now();
        SystemSetting setting = SystemSetting.builder()
                .settingKey(request.getSettingKey())
                .settingValue(request.getSettingValue())
                .category(category)
                .dataType(dataType)
                .description(request.getDescription())
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        SystemSetting saved = repository.save(setting);
        log.info("Setting created: key={}, category={}", saved.getSettingKey(), saved.getCategory());
        return mapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "settings", key = "#key")
    public SystemSettingResponse update(String key, UpdateSettingRequest request) {
        SystemSetting setting = repository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Setting not found with key: " + key));

        if (request.getSettingValue() != null) {
            SettingDataType dataType = request.getDataType() != null
                    ? parseDataType(request.getDataType())
                    : setting.getDataType();
            validateValue(request.getSettingValue(), dataType);
            setting.setSettingValue(request.getSettingValue());
        }

        if (request.getDataType() != null) {
            setting.setDataType(parseDataType(request.getDataType()));
        }

        if (request.getDescription() != null) {
            setting.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            setting.setIsActive(request.getIsActive());
        }

        setting.setUpdatedAt(LocalDateTime.now());
        SystemSetting saved = repository.save(setting);
        log.info("Setting updated: key={}", key);
        return mapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "settings", allEntries = true)
    public List<SystemSettingResponse> bulkUpdateByCategory(
            String category, BulkUpdateSettingsRequest request) {
        SettingCategory settingCategory = parseCategory(category);
        List<SystemSetting> existing = repository.findByCategoryAndIsActiveTrue(settingCategory);
        List<SystemSetting> updated = new ArrayList<>();

        for (BulkUpdateSettingsRequest.BulkUpdateItem item : request.getSettings()) {
            SystemSetting setting = existing.stream()
                    .filter(s -> s.getSettingKey().equals(item.getSettingKey()))
                    .findFirst()
                    .orElse(null);
            if (setting != null && item.getSettingValue() != null) {
                validateValue(item.getSettingValue(), setting.getDataType());
                setting.setSettingValue(item.getSettingValue());
                setting.setUpdatedAt(LocalDateTime.now());
                updated.add(setting);
            }
        }

        if (!updated.isEmpty()) {
            repository.saveAll(updated);
            log.info("Bulk settings updated: category={}, count={}", category, updated.size());
        }

        List<SystemSetting> refreshed = repository.findByCategoryAndIsActiveTrue(settingCategory);
        return mapper.toResponseList(refreshed);
    }

    @Transactional
    @CacheEvict(value = "settings", key = "#key")
    public void delete(String key) {
        SystemSetting setting = repository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Setting not found with key: " + key));
        repository.delete(setting);
        log.info("Setting deleted: key={}", key);
    }

    @Transactional(readOnly = true)
    public PageResponse<SystemSettingResponse> search(String keyword, String category,
                                                      int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "category", "settingKey");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SystemSetting> settingPage;

        if (category != null && !category.isBlank()) {
            SettingCategory settingCategory = parseCategory(category);
            settingPage = repository.findByCategoryAndIsActiveTrue(settingCategory, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            settingPage = repository.findBySettingKeyContainingIgnoreCaseAndIsActiveTrue(keyword, pageable);
        } else {
            settingPage = repository.findByIsActiveTrue(pageable);
        }

        return PageResponse.from(settingPage.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return SettingCategory.VALUES.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    private SettingCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new BadRequestException("Category is required");
        }
        try {
            return SettingCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid category: " + category);
        }
    }

    private SettingDataType parseDataType(String dataType) {
        if (dataType == null || dataType.isBlank()) {
            throw new BadRequestException("Data type is required");
        }
        try {
            return SettingDataType.valueOf(dataType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid data type: " + dataType);
        }
    }

    private void validateValue(String value, SettingDataType dataType) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Setting value is required");
        }
        try {
            switch (dataType) {
                case INTEGER -> Integer.parseInt(value);
                case LONG -> Long.parseLong(value);
                case BOOLEAN -> {
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                        throw new BadRequestException("Invalid boolean value: " + value);
                    }
                }
                case DECIMAL -> new BigDecimal(value);
                case JSON -> {
                    if (!value.startsWith("{") && !value.startsWith("[")) {
                        throw new BadRequestException("Invalid JSON value");
                    }
                }
                case STRING -> {
                    // any string is valid
                }
            }
        } catch (RuntimeException e) {
            if (e.getMessage() != null && !e.getMessage().startsWith("Invalid")) {
                throw new BadRequestException("Value '" + value + "' is not valid for type " + dataType);
            }
            throw e;
        }
    }

}
