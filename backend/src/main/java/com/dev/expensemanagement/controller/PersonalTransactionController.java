package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.dto.request.PersonalTransactionRequest;
import com.dev.expensemanagement.dto.request.SyncGroupExpenseRequest;
import com.dev.expensemanagement.dto.response.PagedResponse;
import com.dev.expensemanagement.dto.response.PersonalDashboardResponse;
import com.dev.expensemanagement.dto.response.PersonalTransactionResponse;
import com.dev.expensemanagement.entity.PersonalTransactionType;
import com.dev.expensemanagement.security.CurrentUserService;
import com.dev.expensemanagement.service.PersonalTransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
public class PersonalTransactionController {

    private final PersonalTransactionService transactionService;
    private final CurrentUserService currentUserService;

    @GetMapping("/personal/dashboard")
    public ResponseEntity<PersonalDashboardResponse> getDashboard(
            @RequestParam(required = false) String month
    ) {
        return ResponseEntity.ok(transactionService.getDashboard(
                currentUserService.requireUserId(),
                month
        ));
    }

    @GetMapping("/transactions")
    public ResponseEntity<PagedResponse<PersonalTransactionResponse>> getTransactions(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) PersonalTransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(transactionService.getTransactions(
                currentUserService.requireUserId(),
                month,
                type,
                categoryId,
                query,
                page,
                size
        ));
    }

    @PostMapping("/transactions")
    public ResponseEntity<PersonalTransactionResponse> createTransaction(
            @Valid @RequestBody PersonalTransactionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(
                currentUserService.requireUserId(),
                request
        ));
    }

    @PostMapping("/groups/{groupId}/sync-personal")
    public ResponseEntity<PersonalTransactionResponse> syncGroupExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody SyncGroupExpenseRequest request
    ) {
        return ResponseEntity.ok(transactionService.syncGroupExpense(
                currentUserService.requireUserId(),
                groupId,
                request
        ));
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<PersonalTransactionResponse> getTransaction(@PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(
                currentUserService.requireUserId(),
                transactionId
        ));
    }

    @PutMapping("/transactions/{transactionId}")
    public ResponseEntity<PersonalTransactionResponse> updateTransaction(
            @PathVariable Long transactionId,
            @Valid @RequestBody PersonalTransactionRequest request
    ) {
        return ResponseEntity.ok(transactionService.updateTransaction(
                currentUserService.requireUserId(),
                transactionId,
                request
        ));
    }

    @DeleteMapping("/transactions/{transactionId}")
    public ResponseEntity<Map<String, Object>> deleteTransaction(@PathVariable Long transactionId) {
        transactionService.deleteTransaction(currentUserService.requireUserId(), transactionId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Xóa giao dịch thành công"));
    }
}
