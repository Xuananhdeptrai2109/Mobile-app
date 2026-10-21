package com.dev.expensemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseExtractionResponse {
    private String name;
    private BigDecimal amount;
    private String note;
    private LocalDate expenseDate;
    private String merchantName;
    private String rawText;
    private String message;
    private boolean success;
}
