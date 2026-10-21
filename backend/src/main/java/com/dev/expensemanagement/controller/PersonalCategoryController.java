package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.dto.response.PersonalCategoryResponse;
import com.dev.expensemanagement.entity.PersonalTransactionType;
import com.dev.expensemanagement.service.PersonalCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PersonalCategoryController {

    private final PersonalCategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<PersonalCategoryResponse>> getCategories(
            @RequestParam(required = false) PersonalTransactionType type
    ) {
        return ResponseEntity.ok(categoryService.getCategories(type));
    }
}
