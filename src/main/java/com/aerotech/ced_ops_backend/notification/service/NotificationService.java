package com.aerotech.ced_ops_backend.notification.service;

import com.aerotech.ced_ops_backend.common.enums.NotificationType;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.notification.dto.response.NotificationResponse;
import com.aerotech.ced_ops_backend.notification.dto.response.UnreadCountResponse;
import com.aerotech.ced_ops_backend.notification.entity.Notification;
import com.aerotech.ced_ops_backend.notification.mapper.NotificationMapper;
import com.aerotech.ced_ops_backend.notification.repository.NotificationRepository;
import com.aerotech.ced_ops_backend.user.entity.User;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(String type, Boolean isRead, int page, int size) {
        Long userId = currentUserId();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Notification> notificationPage;
        if (type != null && !type.isBlank() && isRead != null) {
            NotificationType notificationType = parseType(type);
            notificationPage = notificationRepository
                    .findByRecipientUserIdAndTypeAndIsReadOrderByCreatedAtDesc(userId, notificationType, isRead, pageable);
        } else if (type != null && !type.isBlank()) {
            NotificationType notificationType = parseType(type);
            notificationPage = notificationRepository
                    .findByRecipientUserIdAndTypeOrderByCreatedAtDesc(userId, notificationType, pageable);
        } else if (isRead != null) {
            if (isRead) {
                notificationPage = notificationRepository
                        .findByRecipientUserIdAndIsReadTrueOrderByCreatedAtDesc(userId, pageable);
            } else {
                notificationPage = notificationRepository
                        .findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
            }
        } else {
            notificationPage = notificationRepository
                    .findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return PageResponse.from(notificationPage.map(notificationMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications() {
        Long userId = currentUserId();
        List<Notification> notifications = notificationRepository
                .findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notificationMapper.toResponseList(notifications);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        Long userId = currentUserId();
        long count = notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);
        return UnreadCountResponse.builder()
                .count(count)
                .build();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Long userId = currentUserId();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        if (!notification.getRecipientUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);
        log.info("Notification marked as read: id={}, userId={}", id, userId);
        return notificationMapper.toResponse(saved);
    }

    @Transactional
    public void markAllAsRead() {
        Long userId = currentUserId();
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("All notifications marked as read: userId={}", userId);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserId();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        if (!notification.getRecipientUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }

        notificationRepository.delete(notification);
        log.info("Notification deleted: id={}, userId={}", id, userId);
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResourceNotFoundException("User not found");
        }
        User user = userRepository.findByEmployeeId(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }

    private NotificationType parseType(String type) {
        try {
            return NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
