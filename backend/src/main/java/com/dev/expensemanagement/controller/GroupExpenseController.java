package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.dto.request.CreateGroupExpenseRequest;
import com.dev.expensemanagement.dto.response.GroupExpenseResponse;
import com.dev.expensemanagement.dto.response.GroupExpenseSplitResponse;
import com.dev.expensemanagement.service.GroupExpenseService;
import com.dev.expensemanagement.security.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class GroupExpenseController {

    private final GroupExpenseService groupExpenseService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<?> createExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupExpenseRequest request
    ) {
        try {
            GroupExpenseResponse response = groupExpenseService.createExpense(
                    groupId,
                    currentUserService.requireUserId(),
                    request
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getGroupExpenses(@PathVariable Long groupId) {
        try {
            List<GroupExpenseResponse> response = groupExpenseService.getGroupExpenses(
                    groupId,
                    currentUserService.requireUserId()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(e.getMessage()));
        }
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<?> getExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId
    ) {
        try {
            return ResponseEntity.ok(groupExpenseService.getExpense(
                    groupId,
                    expenseId,
                    currentUserService.requireUserId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(e.getMessage()));
        }
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<?> updateExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @Valid @RequestBody CreateGroupExpenseRequest request
    ) {
        try {
            return ResponseEntity.ok(groupExpenseService.updateExpense(
                    groupId,
                    expenseId,
                    currentUserService.requireUserId(),
                    request
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?> deleteExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId
    ) {
        try {
            groupExpenseService.deleteExpense(groupId, expenseId, currentUserService.requireUserId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Xoa chi tieu thanh cong"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PatchMapping("/{expenseId}/splits/{splitId}/toggle-payment")
    public ResponseEntity<?> toggleSplitPayment(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @PathVariable Long splitId
    ) {
        try {
            GroupExpenseSplitResponse response = groupExpenseService.toggleSplitPayment(
                    groupId,
                    expenseId,
                    splitId,
                    currentUserService.requireUserId()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    private Map<String, Object> error(String message) {
        return Map.of(
                "success", false,
                "message", message
        );
    }
}
