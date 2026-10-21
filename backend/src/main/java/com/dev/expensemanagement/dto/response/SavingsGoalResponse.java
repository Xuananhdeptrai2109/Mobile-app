package com.dev.expensemanagement.dto.response;

import com.dev.expensemanagement.entity.SavingsGoalStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoalResponse {
    private Long goalId;
    private String title;
    private String emoji;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal progressPercentage;
    private LocalDate targetDate;
    private SavingsGoalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
