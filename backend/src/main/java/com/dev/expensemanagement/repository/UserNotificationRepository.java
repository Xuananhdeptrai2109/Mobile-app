package com.dev.expensemanagement.repository;

import com.dev.expensemanagement.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUser_IdOrderByCreatedAtDesc(Long userId);
    long countByUser_IdAndIsReadFalse(Long userId);
    Optional<UserNotification> findByIdAndUser_Id(Long notificationId, Long userId);
    List<UserNotification> findByUser_IdAndIsReadFalse(Long userId);
}
