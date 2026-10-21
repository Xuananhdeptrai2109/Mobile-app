package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.dto.response.NotificationResponse;
import com.dev.expensemanagement.dto.response.UnreadNotificationCountResponse;
import com.dev.expensemanagement.security.CurrentUserService;
import com.dev.expensemanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        return ResponseEntity.ok(notificationService.getNotifications(currentUserService.requireUserId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadNotificationCountResponse> getUnreadCount() {
        return ResponseEntity.ok(new UnreadNotificationCountResponse(
                notificationService.getUnreadCount(currentUserService.requireUserId())
        ));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markRead(
                currentUserService.requireUserId(), notificationId
        ));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead(currentUserService.requireUserId());
        return ResponseEntity.noContent().build();
    }
}
