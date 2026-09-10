package com.smartcampus.maintenance.repository;

import com.smartcampus.maintenance.entity.Notification;
import com.smartcampus.maintenance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<Notification> findTop5ByUserOrderByCreatedAtDesc(User user);
    long countByUserAndReadStatusFalse(User user);
    List<Notification> findByUserAndReadStatusFalse(User user);
}
