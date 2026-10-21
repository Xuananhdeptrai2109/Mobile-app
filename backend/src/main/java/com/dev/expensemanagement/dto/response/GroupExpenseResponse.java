package com.dev.expensemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupExpenseResponse {
    private Long expenseId;
    private Long groupId;
    private String name;
    private BigDecimal amount;
    private Long paidByMemberId;
    private String paidByFullName;
    private String splitMethod;
    private String note;
    private LocalDate expenseDate;
    private String receiptImageUrl;
    private List<GroupExpenseSplitResponse> splits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
    private boolean success;
}
