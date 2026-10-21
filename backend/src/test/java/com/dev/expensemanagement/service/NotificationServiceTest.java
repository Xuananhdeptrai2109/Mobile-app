package com.dev.expensemanagement.service;

import com.dev.expensemanagement.entity.User;
import com.dev.expensemanagement.entity.UserNotification;
import com.dev.expensemanagement.repository.UserNotificationRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class NotificationServiceTest {
    @Test
    void markReadOnlyUpdatesOwnedUnreadNotification() {
        UserNotificationRepository repository = mock(UserNotificationRepository.class);
        NotificationService service = new NotificationService(repository);
        UserNotification notification = notification(4L, false);
        when(repository.findByIdAndUser_Id(4L, 7L)).thenReturn(Optional.of(notification));
        when(repository.save(notification)).thenReturn(notification);

        var response = service.markRead(7L, 4L);

        assertTrue(response.isRead());
        verify(repository).save(notification);
    }

    @Test
    void markAllReadUpdatesOnlyUnreadNotificationsOfCurrentUser() {
        UserNotificationRepository repository = mock(UserNotificationRepository.class);
        NotificationService service = new NotificationService(repository);
        UserNotification first = notification(1L, false);
        UserNotification second = notification(2L, false);
        when(repository.findByUser_IdAndIsReadFalse(7L)).thenReturn(List.of(first, second));

        service.markAllRead(7L);

        assertTrue(first.getIsRead());
        assertTrue(second.getIsRead());
        verify(repository).saveAll(List.of(first, second));
    }

    @Test
    void unreadCountIsScopedToCurrentUser() {
        UserNotificationRepository repository = mock(UserNotificationRepository.class);
        NotificationService service = new NotificationService(repository);
        when(repository.countByUser_IdAndIsReadFalse(7L)).thenReturn(3L);

        assertEquals(3L, service.getUnreadCount(7L));
        verify(repository).countByUser_IdAndIsReadFalse(7L);
    }

    private UserNotification notification(Long id, boolean read) {
        return UserNotification.builder()
                .id(id)
                .user(User.builder().id(7L).build())
                .title("Thông báo")
                .message("Nội dung")
                .isRead(read)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
