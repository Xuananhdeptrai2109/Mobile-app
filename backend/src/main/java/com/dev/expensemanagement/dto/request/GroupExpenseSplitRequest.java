package com.dev.expensemanagement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupExpenseSplitRequest {
    @NotNull(message = "Thành viên chia tiền không được trống")
    private Long memberId;

    @DecimalMin(value = "1", message = "Số tiền chia phải lớn hơn 0")
    @Digits(integer = 13, fraction = 0, message = "Số tiền VND không được có phần lẻ")
    private BigDecimal amount;

    @DecimalMin(value = "0.01", message = "Tỷ lệ phải lớn hơn 0")
    @Digits(integer = 3, fraction = 2, message = "Tỷ lệ có tối đa 2 chữ số thập phân")
    private BigDecimal percentage;
}
