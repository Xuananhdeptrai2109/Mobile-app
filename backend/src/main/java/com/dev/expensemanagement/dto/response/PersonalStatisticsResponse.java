package com.dev.expensemanagement.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalStatisticsResponse {
    private String month;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net;
    private BigDecimal savingsRate;
    private BigDecimal previousMonthExpense;
    private BigDecimal expenseChangePercentage;
    private MonthlyBudgetResponse budget;
    private List<CategoryStatisticResponse> expenseByCategory;
    private List<DailyExpenseResponse> dailyExpenses;
}
