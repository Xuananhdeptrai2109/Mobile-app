package com.dev.expensemanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTaskResponse {
    private Long taskId;
    private Long groupId;
    private String title;
    private Long assigneeMemberId;
    private String assigneeName;
    private LocalDate dueDate;
    private String description;
    @Getter(onMethod_ = @JsonProperty("isCompleted"))
    private boolean isCompleted;
    private String message;
    private boolean success;
}
