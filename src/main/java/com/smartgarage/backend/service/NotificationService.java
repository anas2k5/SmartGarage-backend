package com.smartgarage.backend.service;

import com.smartgarage.backend.model.Notification;

import java.util.List;

public interface NotificationService {

    void create(
            Long userId,
            String title,
            String message,
            String type
    );

    List<Notification> getMyNotifications(Long userId);

    void markAsRead(Long id);

    long getUnreadCount(Long userId);
    void markAllAsRead(Long userId);
    void notifyAdmins(
            String title,
            String message,
            String type
    );

}
