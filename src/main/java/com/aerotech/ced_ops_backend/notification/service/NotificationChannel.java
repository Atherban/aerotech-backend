package com.aerotech.ced_ops_backend.notification.service;

import com.aerotech.ced_ops_backend.notification.entity.Notification;
import com.aerotech.ced_ops_backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationChannel {

    private final NotificationRepository notificationRepository;

    public void send(Notification notification) {
        notificationRepository.save(notification);
    }

}
