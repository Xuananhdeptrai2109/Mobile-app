package com.dev.expensemanagement.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyExpenseResponse {
    private LocalDate date;
    private BigDecimal amount;
}
