package com.dev.expensemanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateGroupSettlementRequest {
    @NotNull(message = "Người trả không được để trống")
    private Long fromMemberId;

    @NotNull(message = "Người nhận không được để trống")
    private Long toMemberId;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;
}
