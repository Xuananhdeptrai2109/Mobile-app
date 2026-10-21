package com.dev.expensemanagement.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoalRequest {
    @NotBlank(message = "Tên mục tiêu không được để trống")
    @Size(max = 160, message = "Tên mục tiêu tối đa 160 ký tự")
    private String title;

    @Size(max = 16, message = "Biểu tượng không hợp lệ")
    private String emoji;

    @NotNull(message = "Số tiền mục tiêu không được để trống")
    @DecimalMin(value = "1", message = "Số tiền mục tiêu phải lớn hơn 0")
    @Digits(integer = 13, fraction = 0, message = "Số tiền VND không được có phần lẻ")
    private BigDecimal targetAmount;

    private LocalDate targetDate;
}
