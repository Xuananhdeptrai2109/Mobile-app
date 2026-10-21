package com.dev.expensemanagement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGroupRequest {

    @Size(min = 1, max = 120, message = "Ten nhom phai tu 1 den 120 ky tu")
    private String name;

    @Size(max = 500, message = "Mo ta toi da 500 ky tu")
    private String description;

    @Size(max = 2800000, message = "Anh nhom toi da 2 MB")
    private String imageBase64;

    @DecimalMin(value = "0.0", inclusive = true, message = "Ngan sach phai lon hon hoac bang 0")
    @Digits(integer = 13, fraction = 0, message = "Ngan sach VND khong duoc co phan le")
    private BigDecimal budget;
}
