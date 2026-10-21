package com.dev.expensemanagement.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBudgetResponse {
    private Long budgetId;
    private String month;
    private BigDecimal amount;
    private BigDecimal spent;
    private BigDecimal remaining;
    private BigDecimal usagePercentage;
    private boolean exceeded;
}
