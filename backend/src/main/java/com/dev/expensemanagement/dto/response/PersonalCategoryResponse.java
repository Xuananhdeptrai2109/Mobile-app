package com.dev.expensemanagement.dto.response;

import com.dev.expensemanagement.entity.PersonalTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalCategoryResponse {
    private Long categoryId;
    private String name;
    private PersonalTransactionType type;
    private String iconName;
    private String colorCode;
}
