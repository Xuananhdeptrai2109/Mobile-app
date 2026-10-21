package com.dev.expensemanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGroupMemberRequest {

    @NotBlank(message = "Ten thanh vien khong duoc trong")
    @Size(max = 255, message = "Ten thanh vien toi da 255 ky tu")
    private String name;
}
