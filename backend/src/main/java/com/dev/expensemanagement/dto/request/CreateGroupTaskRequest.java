package com.dev.expensemanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGroupTaskRequest {
    @NotBlank(message = "Tiêu đề nhiệm vụ không được trống")
    @Size(max = 255, message = "Tiêu đề nhiệm vụ tối đa 255 ký tự")
    private String title;
    private Long assigneeMemberId;
    private LocalDate dueDate;
    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;
}
