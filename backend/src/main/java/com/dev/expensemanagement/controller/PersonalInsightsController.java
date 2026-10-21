package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.dto.request.MonthlyBudgetRequest;
import com.dev.expensemanagement.dto.response.*;
import com.dev.expensemanagement.security.CurrentUserService;
import com.dev.expensemanagement.service.PersonalInsightsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/personal")
@RequiredArgsConstructor
public class PersonalInsightsController {
    private final PersonalInsightsService insightsService;
    private final CurrentUserService currentUserService;

    @GetMapping("/statistics")
    public ResponseEntity<PersonalStatisticsResponse> getStatistics(
            @RequestParam(required = false) String month
    ) {
        return ResponseEntity.ok(insightsService.getStatistics(currentUserService.requireUserId(), month));
    }

    @GetMapping("/budgets/{month}")
    public ResponseEntity<MonthlyBudgetResponse> getBudget(@PathVariable String month) {
        return ResponseEntity.ok(insightsService.getBudget(currentUserService.requireUserId(), month));
    }

    @PutMapping("/budgets/{month}")
    public ResponseEntity<MonthlyBudgetResponse> upsertBudget(
            @PathVariable String month,
            @Valid @RequestBody MonthlyBudgetRequest request
    ) {
        return ResponseEntity.ok(insightsService.upsertBudget(
                currentUserService.requireUserId(), month, request
        ));
    }

    @DeleteMapping("/budgets/{month}")
    public ResponseEntity<Map<String, Object>> deleteBudget(@PathVariable String month) {
        insightsService.deleteBudget(currentUserService.requireUserId(), month);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa ngân sách tháng"));
    }
}
