package com.aerotech.ced_ops_backend.notification.repository;

import com.aerotech.ced_ops_backend.common.enums.NotificationType;
import com.aerotech.ced_ops_backend.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);

    Page<Notification> findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(
            Long recipientUserId, Pageable pageable);

    List<Notification> findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientUserId);

    long countByRecipientUserIdAndIsReadFalse(Long recipientUserId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now " +
           "WHERE n.recipientUserId = :recipientUserId AND n.isRead = false")
    int markAllAsRead(@Param("recipientUserId") Long recipientUserId,
                      @Param("now") LocalDateTime now);

    Page<Notification> findByRecipientUserIdAndTypeOrderByCreatedAtDesc(
            Long recipientUserId, NotificationType type, Pageable pageable);

    Page<Notification> findByRecipientUserIdAndIsReadTrueOrderByCreatedAtDesc(
            Long recipientUserId, Pageable pageable);

    Page<Notification> findByRecipientUserIdAndTypeAndIsReadOrderByCreatedAtDesc(
            Long recipientUserId, NotificationType type, Boolean isRead, Pageable pageable);

}
