package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.CreateGroupSettlementRequest;
import com.dev.expensemanagement.entity.*;
import com.dev.expensemanagement.repository.*;
import com.dev.expensemanagement.security.GroupAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GroupSettlementServiceTest {
    private GroupSettlementRepository settlementRepository;
    private GroupMemberRepository memberRepository;
    private GroupExpenseSplitRepository splitRepository;
    private UserRepository userRepository;
    private GroupAuthorizationService authorizationService;
    private GroupActivityService activityService;
    private GroupSettlementService service;

    @BeforeEach
    void setUp() {
        settlementRepository = mock(GroupSettlementRepository.class);
        memberRepository = mock(GroupMemberRepository.class);
        splitRepository = mock(GroupExpenseSplitRepository.class);
        userRepository = mock(UserRepository.class);
        authorizationService = mock(GroupAuthorizationService.class);
        activityService = mock(GroupActivityService.class);
        service = new GroupSettlementService(
                settlementRepository,
                memberRepository,
                splitRepository,
                userRepository,
                authorizationService,
                activityService
        );
    }

    @Test
    void recordsSettlementWithoutRewritingOriginalSplit() {
        Fixture fixture = fixture();
        CreateGroupSettlementRequest request = request(11L, 12L, 100_000);
        when(settlementRepository.save(any(GroupSettlement.class))).thenAnswer(invocation -> {
            GroupSettlement item = invocation.getArgument(0);
            item.setId(41L);
            item.setSettledAt(LocalDateTime.now());
            return item;
        });

        var response = service.settle(5L, 7L, request);

        assertEquals(41L, response.getSettlementId());
        assertEquals(new BigDecimal("100000"), response.getAmount());
        assertEquals(11L, response.getFromMemberId());
        assertEquals(12L, response.getToMemberId());
        assertEquals(false, fixture.split().isPaid());
        verify(activityService).record(
                any(), eq(7L), eq("SETTLEMENT"), any(), any(),
                eq("GROUP_SETTLEMENT"), eq(41L), eq(true)
        );
    }

    @Test
    void rejectsPaymentAboveCurrentDebt() {
        fixture();
        var error = assertThrows(
                IllegalArgumentException.class,
                () -> service.settle(5L, 7L, request(11L, 12L, 100_001))
        );
        assertEquals("Khoản thanh toán vượt quá công nợ hiện tại", error.getMessage());
        verify(settlementRepository, never()).save(any());
    }

    private Fixture fixture() {
        User creator = User.builder().id(7L).build();
        ExpenseGroup group = ExpenseGroup.builder().id(5L).name("Trip").createdBy(creator).build();
        GroupMember debtor = GroupMember.builder().id(11L).group(group).name("An").build();
        GroupMember creditor = GroupMember.builder().id(12L).group(group).name("Bình").build();
        GroupExpense expense = GroupExpense.builder().id(21L).group(group).paidBy(creditor).build();
        GroupExpenseSplit split = GroupExpenseSplit.builder()
                .id(31L)
                .expense(expense)
                .member(debtor)
                .splitAmount(new BigDecimal("100000"))
                .isPaid(false)
                .build();

        when(memberRepository.findByIdAndGroup_Id(11L, 5L)).thenReturn(Optional.of(debtor));
        when(memberRepository.findByIdAndGroup_Id(12L, 5L)).thenReturn(Optional.of(creditor));
        when(memberRepository.findByGroup_Id(5L)).thenReturn(List.of(debtor, creditor));
        when(splitRepository.findByExpense_Group_Id(5L)).thenReturn(List.of(split));
        when(settlementRepository.findByGroup_IdOrderBySettledAtDesc(5L)).thenReturn(List.of());
        when(userRepository.findById(7L)).thenReturn(Optional.of(creator));
        when(authorizationService.requireMember(5L, 7L)).thenReturn(debtor);
        return new Fixture(split);
    }

    private CreateGroupSettlementRequest request(Long fromId, Long toId, long amount) {
        CreateGroupSettlementRequest request = new CreateGroupSettlementRequest();
        request.setFromMemberId(fromId);
        request.setToMemberId(toId);
        request.setAmount(BigDecimal.valueOf(amount));
        return request;
    }

    private record Fixture(GroupExpenseSplit split) {}
}
