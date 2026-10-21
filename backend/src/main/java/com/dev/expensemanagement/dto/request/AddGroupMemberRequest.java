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
public class AddGroupMemberRequest {

    @NotBlank(message = "Tên thành viên không được trống")
    @Size(max = 255, message = "Tên thành viên tối đa 255 ký tự")
    private String name;
}
