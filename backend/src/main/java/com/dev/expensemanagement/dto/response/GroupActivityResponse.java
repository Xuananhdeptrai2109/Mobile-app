package com.dev.expensemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupActivityResponse {
    private Long activityId;
    private Long groupId;
    private String actorName;
    private String type;
    private String title;
    private String description;
    private String relatedEntityType;
    private Long relatedEntityId;
    private LocalDateTime createdAt;
}
