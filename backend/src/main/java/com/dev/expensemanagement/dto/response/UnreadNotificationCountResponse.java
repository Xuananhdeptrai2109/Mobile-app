package com.dev.expensemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnreadNotificationCountResponse {
    private long unreadCount;
}
