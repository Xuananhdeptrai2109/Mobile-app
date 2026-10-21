package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.dto.request.*;
import com.dev.expensemanagement.dto.response.SavingsGoalResponse;
import com.dev.expensemanagement.security.CurrentUserService;
import com.dev.expensemanagement.service.PersonalSavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/savings-goals")
@RequiredArgsConstructor
public class PersonalSavingsGoalController {
    private final PersonalSavingsGoalService goalService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<SavingsGoalResponse>> getGoals(
            @RequestParam(defaultValue = "false") boolean includeCompleted
    ) {
        return ResponseEntity.ok(goalService.getGoals(currentUserService.requireUserId(), includeCompleted));
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(@Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createGoal(currentUserService.requireUserId(), request));
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<SavingsGoalResponse> getGoal(@PathVariable Long goalId) {
        return ResponseEntity.ok(goalService.getGoal(currentUserService.requireUserId(), goalId));
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @PathVariable Long goalId,
            @Valid @RequestBody SavingsGoalRequest request
    ) {
        return ResponseEntity.ok(goalService.updateGoal(currentUserService.requireUserId(), goalId, request));
    }

    @PostMapping("/{goalId}/contributions")
    public ResponseEntity<SavingsGoalResponse> contribute(
            @PathVariable Long goalId,
            @Valid @RequestBody SavingsContributionRequest request
    ) {
        return ResponseEntity.ok(goalService.contribute(
                currentUserService.requireUserId(), goalId, request
        ));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Map<String, Object>> deleteGoal(@PathVariable Long goalId) {
        goalService.deleteGoal(currentUserService.requireUserId(), goalId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa mục tiêu tiết kiệm"));
    }
}
