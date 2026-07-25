package com.aerotech.ced_ops_backend.settings.mapper;

import com.aerotech.ced_ops_backend.settings.dto.response.SystemSettingResponse;
import com.aerotech.ced_ops_backend.settings.entity.SystemSetting;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SystemSettingMapper {

    public SystemSettingResponse toResponse(SystemSetting setting) {
        if (setting == null) {
            return null;
        }
        return SystemSettingResponse.builder()
                .id(setting.getId())
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .category(setting.getCategory() != null ? setting.getCategory().name() : null)
                .dataType(setting.getDataType() != null ? setting.getDataType().name() : null)
                .description(setting.getDescription())
                .isActive(setting.getIsActive())
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }

    public List<SystemSettingResponse> toResponseList(List<SystemSetting> settings) {
        return settings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
