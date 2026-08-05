package com.aerotech.ced_ops_backend.notification.service;

import com.aerotech.ced_ops_backend.common.enums.NotificationPriority;
import com.aerotech.ced_ops_backend.common.enums.NotificationType;
import com.aerotech.ced_ops_backend.notification.entity.Notification;
import com.aerotech.ced_ops_backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * In-app notification channel used by business flows to deliver notifications.
 *
 * <p>The existing {@link #send(Notification)} primitive persists a fully-built
 * {@link Notification}. The convenience {@code notify(...)} overloads build a
 * complete, unread notification with sensible defaults (priority derived from
 * type, {@code createdAt} stamped, {@code isRead=false}) and persist it.
 */
@Component
@RequiredArgsConstructor
public class NotificationChannel {

    private final NotificationRepository notificationRepository;

    public void send(Notification notification) {
        notificationRepository.save(notification);
    }

    /**
     * Builds and sends a notification, deriving the default priority from the type.
     */
    public void notify(
            NotificationType type,
            Long recipientUserId,
            String title,
            String message,
            String relatedModule,
            String relatedEntityId,
            String metadata
    ) {
        notify(type, defaultPriority(type), recipientUserId,
                title, message, relatedModule, relatedEntityId, metadata);
    }

    /**
     * Builds and sends a notification with an explicit priority.
     */
    public void notify(
            NotificationType type,
            NotificationPriority priority,
            Long recipientUserId,
            String title,
            String message,
            String relatedModule,
            String relatedEntityId,
            String metadata
    ) {
        Notification notification = Notification.builder()
                .recipientUserId(recipientUserId)
                .title(title)
                .message(message)
                .type(type)
                .relatedModule(relatedModule)
                .relatedEntityId(relatedEntityId)
                .priority(priority)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .metadata(metadata)
                .build();
        send(notification);
    }

    private NotificationPriority defaultPriority(NotificationType type) {
        return switch (type) {
            case REPORT_APPROVED, REPORT_REJECTED, REPORT_RETURNED -> NotificationPriority.HIGH;
            case REPORT_SUBMITTED, PENDING_APPROVAL, APPROVAL_REMINDER,
                 WELCOME, PASSWORD_CHANGED, USER_CREATED, USER_ACTIVATED,
                 USER_DEACTIVATED, ROLE_CHANGED, ATTACHMENT_UPLOADED,
                 MAINTENANCE_NOTICE -> NotificationPriority.MEDIUM;
            case REPORT_CREATED -> NotificationPriority.LOW;
        };
    }

}
