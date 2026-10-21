package com.dev.expensemanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    @NotBlank(message = "Email hoặc số điện thoại không được trống")
    private String emailOrPhoneNumber;
    
    @NotBlank(message = "Mật khẩu không được trống")
    @Pattern(regexp = "^.{6,}$", message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
}
