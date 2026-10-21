package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.response.NotificationResponse;
import com.dev.expensemanagement.entity.User;
import com.dev.expensemanagement.entity.UserNotification;
import com.dev.expensemanagement.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final UserNotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUser_IdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markRead(Long userId, Long notificationId) {
        UserNotification notification = notificationRepository.findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Thông báo không tồn tại"));
        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification = notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<UserNotification> unread = notificationRepository.findByUser_IdAndIsReadFalse(userId);
        unread.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void createWelcomeNotification(User user) {
        notificationRepository.save(UserNotification.builder()
                .user(user)
                .title("Chào mừng đến với MoneyCare")
                .message("Tài khoản của bạn đã sẵn sàng. Hãy bắt đầu bằng giao dịch đầu tiên.")
                .isRead(false)
                .build());
    }

    @Transactional
    public void createNotification(User user, String title, String message) {
        if (user == null) return;
        notificationRepository.save(UserNotification.builder()
                .user(user)
                .title(title)
                .message(message)
                .isRead(false)
                .build());
    }

    @Transactional
    public void createNotifications(List<User> users, String title, String message) {
        users.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(User::getId, user -> user, (first, ignored) -> first))
                .values()
                .forEach(user -> createNotification(user, title, message));
    }

    private NotificationResponse toResponse(UserNotification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(Boolean.TRUE.equals(notification.getIsRead()))
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
