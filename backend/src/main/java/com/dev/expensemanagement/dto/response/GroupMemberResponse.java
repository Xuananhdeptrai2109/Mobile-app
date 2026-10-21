package com.dev.expensemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMemberResponse {
    private Long memberId;
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private LocalDateTime joinedAt;
}
