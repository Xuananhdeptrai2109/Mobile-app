package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.response.PersonalCategoryResponse;
import com.dev.expensemanagement.entity.PersonalCategory;
import com.dev.expensemanagement.entity.PersonalTransactionType;
import com.dev.expensemanagement.repository.PersonalCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalCategoryService {

    private final PersonalCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<PersonalCategoryResponse> getCategories(PersonalTransactionType type) {
        List<PersonalCategory> categories = type == null
                ? categoryRepository.findByIsActiveTrueOrderByTypeAscDisplayOrderAsc()
                : categoryRepository.findByTypeAndIsActiveTrueOrderByDisplayOrderAsc(type);
        return categories.stream().map(this::toResponse).toList();
    }

    private PersonalCategoryResponse toResponse(PersonalCategory category) {
        return PersonalCategoryResponse.builder()
                .categoryId(category.getId())
                .name(category.getName())
                .type(category.getType())
                .iconName(category.getIconName())
                .colorCode(category.getColorCode())
                .build();
    }
}
