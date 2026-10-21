package com.dev.expensemanagement.dto.response;

import com.dev.expensemanagement.entity.PersonalTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalTransactionResponse {
    private Long transactionId;
    private String title;
    private BigDecimal amount;
    private PersonalTransactionType type;
    private Long categoryId;
    private String categoryName;
    private String categoryIconName;
    private String categoryColorCode;
    private Long walletId;
    private String walletName;
    private String note;
    private String receiptImageUrl;
    private LocalDateTime transactionTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
