package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.dto.request.CreateWalletRequest;
import com.dev.expensemanagement.dto.request.UpdateWalletRequest;
import com.dev.expensemanagement.dto.response.WalletResponse;
import com.dev.expensemanagement.security.CurrentUserService;
import com.dev.expensemanagement.service.PersonalWalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class PersonalWalletController {

    private final PersonalWalletService walletService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getWallets() {
        return ResponseEntity.ok(walletService.getWallets(currentUserService.requireUserId()));
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.createWallet(currentUserService.requireUserId(), request));
    }

    @PatchMapping("/{walletId}")
    public ResponseEntity<WalletResponse> updateWallet(
            @PathVariable Long walletId,
            @Valid @RequestBody UpdateWalletRequest request
    ) {
        return ResponseEntity.ok(walletService.updateWallet(
                currentUserService.requireUserId(),
                walletId,
                request
        ));
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<Map<String, Object>> deleteWallet(@PathVariable Long walletId) {
        walletService.deleteWallet(currentUserService.requireUserId(), walletId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Xóa ví thành công"));
    }
}
