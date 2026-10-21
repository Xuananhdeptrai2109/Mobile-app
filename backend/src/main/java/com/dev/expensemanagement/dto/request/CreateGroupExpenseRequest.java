package com.dev.expensemanagement.dto.request;

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
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGroupExpenseRequest {

    @NotBlank(message = "Ten chi tieu khong duoc trong")
    @Size(max = 160, message = "Ten chi tieu toi da 160 ky tu")
    private String name;

    @NotNull(message = "So tien khong duoc trong")
    @DecimalMin(value = "1", message = "So tien phai lon hon 0")
    @Digits(integer = 13, fraction = 0, message = "So tien VND khong duoc co phan le")
    private BigDecimal amount;

    @NotNull(message = "Nguoi thanh toan khong duoc trong")
    private Long paidByMemberId;

    private List<Long> splitMemberIds;

    /** EQUAL (default), PERCENTAGE, or CUSTOM. */
    private String splitMethod;

    @jakarta.validation.Valid
    private List<GroupExpenseSplitRequest> splitDetails;

    @Size(max = 500, message = "Ghi chu toi da 500 ky tu")
    private String note;

    @NotNull(message = "Ngay chi tieu khong duoc trong")
    private LocalDate expenseDate;

    private String receiptImageUrl;
}
