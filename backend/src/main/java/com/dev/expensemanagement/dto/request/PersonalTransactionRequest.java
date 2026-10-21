package com.dev.expensemanagement.dto.request;

import com.dev.expensemanagement.entity.PersonalTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class PersonalTransactionRequest {
    @NotBlank(message = "Tiêu đề giao dịch không được trống")
    @Size(max = 160, message = "Tiêu đề giao dịch tối đa 160 ký tự")
    private String title;

    @NotNull(message = "Số tiền không được trống")
    @DecimalMin(value = "1", message = "Số tiền phải lớn hơn 0")
    @Digits(integer = 13, fraction = 0, message = "Số tiền VND không được có phần lẻ")
    private BigDecimal amount;

    @NotNull(message = "Loại giao dịch không được trống")
    private PersonalTransactionType type;

    @NotNull(message = "Danh mục không được trống")
    private Long categoryId;

    @NotNull(message = "Ví không được trống")
    private Long walletId;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;

    @Size(max = 1000, message = "Đường dẫn hóa đơn tối đa 1000 ký tự")
    private String receiptImageUrl;

    @NotNull(message = "Thời gian giao dịch không được trống")
    private LocalDateTime transactionTime;
}
