package com.dev.expensemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalDashboardResponse {
    private String month;
    private BigDecimal balance;
    private BigDecimal income;
    private BigDecimal expense;
    private List<PersonalTransactionResponse> recentTransactions;
}
