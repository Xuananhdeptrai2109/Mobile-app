package com.dev.expensemanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SyncGroupExpenseRequest {
    @NotNull(message = "Ví không được để trống")
    private Long walletId;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
}
