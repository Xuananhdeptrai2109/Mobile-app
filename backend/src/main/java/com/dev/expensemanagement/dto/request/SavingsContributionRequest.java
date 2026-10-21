package com.dev.expensemanagement.dto.request;

import com.dev.expensemanagement.entity.SavingsOperation;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsContributionRequest {
    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "1", message = "Số tiền phải lớn hơn 0")
    @Digits(integer = 13, fraction = 0, message = "Số tiền VND không được có phần lẻ")
    private BigDecimal amount;

    @NotNull(message = "Thao tác không được để trống")
    private SavingsOperation operation;
}
