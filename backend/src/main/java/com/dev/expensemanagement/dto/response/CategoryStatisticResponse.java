package com.dev.expensemanagement.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryStatisticResponse {
    private Long categoryId;
    private String categoryName;
    private String iconName;
    private String colorCode;
    private BigDecimal amount;
    private BigDecimal percentage;
    private long transactionCount;
}
