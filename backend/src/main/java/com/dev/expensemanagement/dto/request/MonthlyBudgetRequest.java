package com.dev.expensemanagement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBudgetRequest {
    @NotNull(message = "Ngân sách không được để trống")
    @DecimalMin(value = "1", message = "Ngân sách phải lớn hơn 0")
    @Digits(integer = 13, fraction = 0, message = "Ngân sách VND không được có phần lẻ")
    private BigDecimal amount;
}
