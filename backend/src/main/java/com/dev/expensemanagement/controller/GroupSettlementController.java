package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.dto.request.CreateGroupSettlementRequest;
import com.dev.expensemanagement.dto.response.GroupSettlementResponse;
import com.dev.expensemanagement.security.CurrentUserService;
import com.dev.expensemanagement.service.GroupSettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups/{groupId}/settlements")
@RequiredArgsConstructor
public class GroupSettlementController {
    private final GroupSettlementService settlementService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<GroupSettlementResponse>> getSettlements(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.getSettlements(
                groupId,
                currentUserService.requireUserId()
        ));
    }

    @PostMapping
    public ResponseEntity<GroupSettlementResponse> settle(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupSettlementRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementService.settle(
                groupId,
                currentUserService.requireUserId(),
                request
        ));
    }
}
