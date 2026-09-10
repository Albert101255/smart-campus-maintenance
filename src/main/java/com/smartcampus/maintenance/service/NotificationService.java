package com.smartcampus.maintenance.service;

import com.smartcampus.maintenance.entity.Notification;
import com.smartcampus.maintenance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    Notification sendNotification(User recipient, String title, String message, String linkUrl);
    List<Notification> getUserNotifications(User user);
    Page<Notification> getUserNotifications(User user, Pageable pageable);
    List<Notification> getRecentUserNotifications(User user);
    long getUnreadCount(User user);
    void markAsRead(Long notificationId, User user);
    void markAllAsRead(User user);
}
