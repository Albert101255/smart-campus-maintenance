package com.smartcampus.maintenance.service.impl;

import com.smartcampus.maintenance.entity.Notification;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.repository.NotificationRepository;
import com.smartcampus.maintenance.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public Notification sendNotification(User recipient, String title, String message, String linkUrl) {
        Notification notification = new Notification(recipient, title, message, linkUrl);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getRecentUserNotifications(User user) {
        return notificationRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadStatusFalse(user);
    }

    @Override
    public void markAsRead(Long notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getId().equals(user.getId())) {
                n.setReadStatus(true);
                notificationRepository.save(n);
            }
        });
    }

    @Override
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndReadStatusFalse(user);
        for (Notification n : unread) {
            n.setReadStatus(true);
        }
        notificationRepository.saveAll(unread);
    }
}
