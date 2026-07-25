package com.aerotech.ced_ops_backend.notification.mapper;

import com.aerotech.ced_ops_backend.notification.dto.response.NotificationResponse;
import com.aerotech.ced_ops_backend.notification.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType() != null ? notification.getType().name() : null)
                .relatedModule(notification.getRelatedModule())
                .relatedEntityId(notification.getRelatedEntityId())
                .priority(notification.getPriority() != null ? notification.getPriority().name() : null)
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .metadata(notification.getMetadata())
                .build();
    }

    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
