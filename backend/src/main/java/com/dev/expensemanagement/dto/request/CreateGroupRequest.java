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
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGroupRequest {

    @NotBlank(message = "Ten nhom khong duoc trong")
    @Size(max = 120, message = "Ten nhom toi da 120 ky tu")
    private String name;

    @Size(max = 500, message = "Mo ta toi da 500 ky tu")
    private String description;

    // Base64 string (optionally including its data-URL prefix). 2.8 MB of Base64
    // represents roughly a 2 MB source image and keeps requests bounded.
    @Size(max = 2800000, message = "Anh nhom toi da 2 MB")
    private String imageBase64;

    @NotNull(message = "Ngan sach khong duoc trong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Ngan sach phai lon hon hoac bang 0")
    @Digits(integer = 13, fraction = 0, message = "Ngan sach VND khong duoc co phan le")
    private BigDecimal budget;

    @Size(max = 50, message = "Moi nhom chi duoc them toi da 50 thanh vien ban dau")
    private List<
            @NotBlank(message = "Ten thanh vien khong duoc trong")
            @Size(max = 255, message = "Ten thanh vien toi da 255 ky tu") String
            > memberNames;

}
