package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.CreateGroupExpenseRequest;
import com.dev.expensemanagement.dto.request.GroupExpenseSplitRequest;
import com.dev.expensemanagement.entity.ExpenseGroup;
import com.dev.expensemanagement.entity.GroupExpense;
import com.dev.expensemanagement.entity.GroupExpenseSplit;
import com.dev.expensemanagement.entity.GroupMember;
import com.dev.expensemanagement.repository.ExpenseGroupRepository;
import com.dev.expensemanagement.repository.GroupExpenseRepository;
import com.dev.expensemanagement.repository.GroupExpenseSplitRepository;
import com.dev.expensemanagement.repository.GroupMemberRepository;
import com.dev.expensemanagement.security.GroupAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

class GroupExpenseServiceScopeTest {

    private ExpenseGroupRepository groupRepository;
    private GroupExpenseRepository expenseRepository;
    private GroupMemberRepository memberRepository;
    private GroupExpenseSplitRepository splitRepository;
    private GroupAuthorizationService authorizationService;
    private GroupExpenseService expenseService;

    @BeforeEach
    void setUp() {
        groupRepository = mock(ExpenseGroupRepository.class);
        expenseRepository = mock(GroupExpenseRepository.class);
        memberRepository = mock(GroupMemberRepository.class);
        splitRepository = mock(GroupExpenseSplitRepository.class);
        authorizationService = mock(GroupAuthorizationService.class);
        expenseService = new GroupExpenseService(
                groupRepository,
                expenseRepository,
                splitRepository,
                memberRepository,
                authorizationService,
                mock(GroupActivityService.class)
        );
    }

    @Test
    void expenseLookupIsScopedToRequestedGroup() {
        when(groupRepository.findById(10L)).thenReturn(Optional.of(ExpenseGroup.builder().id(10L).build()));
        when(expenseRepository.findByIdAndGroup_Id(30L, 10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> expenseService.getExpense(10L, 30L, 20L));

        verify(authorizationService).requireMember(10L, 20L);
        verify(expenseRepository).findByIdAndGroup_Id(30L, 10L);
    }

    @Test
    void createRejectsPayerFromAnotherGroup() {
        when(groupRepository.findById(10L)).thenReturn(Optional.of(ExpenseGroup.builder().id(10L).build()));
        when(memberRepository.findByIdAndGroup_Id(99L, 10L)).thenReturn(Optional.empty());
        CreateGroupExpenseRequest request = CreateGroupExpenseRequest.builder()
                .name("Dinner")
                .amount(BigDecimal.valueOf(100_000))
                .paidByMemberId(99L)
                .splitMemberIds(List.of(99L))
                .expenseDate(LocalDate.of(2026, 10, 7))
                .build();

        assertThrows(IllegalArgumentException.class, () -> expenseService.createExpense(10L, 20L, request));

        verify(authorizationService).requireMember(10L, 20L);
        verify(memberRepository).findByIdAndGroup_Id(99L, 10L);
    }

    @Test
    void deleteExpenseRequiresMembershipAndUsesGroupScopedLookup() {
        GroupExpense expense = GroupExpense.builder().id(30L).build();
        when(expenseRepository.findByIdAndGroup_Id(30L, 10L)).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(10L, 30L, 20L);

        verify(authorizationService).requireMember(10L, 20L);
        verify(expenseRepository).findByIdAndGroup_Id(30L, 10L);
        verify(splitRepository).findByExpense_Id(30L);
        verify(expenseRepository).delete(expense);
    }

    @Test
    void ownerCanTogglePaymentUsingGroupScopedSplitLookup() {
        GroupExpenseSplit split = GroupExpenseSplit.builder()
                .id(40L)
                .expense(GroupExpense.builder().id(30L).build())
                .member(GroupMember.builder().id(2L).name("Lan").build())
                .splitAmount(BigDecimal.valueOf(50_000))
                .isPaid(false)
                .build();
        when(splitRepository.findByIdAndExpense_IdAndExpense_Group_Id(40L, 30L, 10L))
                .thenReturn(Optional.of(split));
        when(splitRepository.save(split)).thenReturn(split);

        var response = expenseService.toggleSplitPayment(10L, 30L, 40L, 20L);

        assertTrue(response.isPaid());
        verify(authorizationService).requireOwner(10L, 20L);
        verify(splitRepository).findByIdAndExpense_IdAndExpense_Group_Id(40L, 30L, 10L);
    }

    @Test
    void splitsVndIntoWholeUnitsAndAssignsRemainderToLastMember() {
        ExpenseGroup group = ExpenseGroup.builder().id(10L).build();
        GroupMember first = GroupMember.builder().id(1L).group(group).name("An").build();
        GroupMember second = GroupMember.builder().id(2L).group(group).name("Binh").build();
        GroupMember third = GroupMember.builder().id(3L).group(group).name("Chi").build();
        GroupExpense savedExpense = GroupExpense.builder()
                .id(30L)
                .group(group)
                .name("Dinner")
                .amount(BigDecimal.valueOf(100))
                .paidBy(first)
                .expenseDate(LocalDate.of(2026, 10, 14))
                .build();

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(memberRepository.findByIdAndGroup_Id(1L, 10L)).thenReturn(Optional.of(first));
        when(memberRepository.findByIdAndGroup_Id(2L, 10L)).thenReturn(Optional.of(second));
        when(memberRepository.findByIdAndGroup_Id(3L, 10L)).thenReturn(Optional.of(third));
        when(expenseRepository.save(org.mockito.ArgumentMatchers.any(GroupExpense.class)))
                .thenReturn(savedExpense);
        when(splitRepository.findByExpense_Id(30L)).thenReturn(List.of());

        CreateGroupExpenseRequest request = CreateGroupExpenseRequest.builder()
                .name("Dinner")
                .amount(BigDecimal.valueOf(100))
                .paidByMemberId(1L)
                .splitMemberIds(List.of(1L, 2L, 3L))
                .expenseDate(LocalDate.of(2026, 10, 14))
                .build();

        expenseService.createExpense(10L, 20L, request);

        ArgumentCaptor<GroupExpenseSplit> captor = ArgumentCaptor.forClass(GroupExpenseSplit.class);
        verify(splitRepository, times(3)).save(captor.capture());
        assertEquals(
                List.of(new BigDecimal("33"), new BigDecimal("33"), new BigDecimal("34")),
                captor.getAllValues().stream().map(GroupExpenseSplit::getSplitAmount).toList()
        );
    }

    @Test
    void createsCustomSplitsWhenAmountsMatchExpenseTotal() {
        ExpenseGroup group = ExpenseGroup.builder().id(10L).build();
        GroupMember first = GroupMember.builder().id(1L).group(group).name("An").build();
        GroupMember second = GroupMember.builder().id(2L).group(group).name("Binh").build();
        GroupExpense savedExpense = GroupExpense.builder()
                .id(31L).group(group).name("Hotel").amount(BigDecimal.valueOf(100_000))
                .paidBy(first).expenseDate(LocalDate.of(2026, 10, 18)).build();
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(memberRepository.findByIdAndGroup_Id(1L, 10L)).thenReturn(Optional.of(first));
        when(memberRepository.findByIdAndGroup_Id(2L, 10L)).thenReturn(Optional.of(second));
        when(expenseRepository.save(any(GroupExpense.class))).thenReturn(savedExpense);
        when(splitRepository.findByExpense_Id(31L)).thenReturn(List.of());

        CreateGroupExpenseRequest request = CreateGroupExpenseRequest.builder()
                .name("Hotel").amount(BigDecimal.valueOf(100_000)).paidByMemberId(1L)
                .splitMethod("CUSTOM")
                .splitDetails(List.of(
                        GroupExpenseSplitRequest.builder().memberId(1L).amount(BigDecimal.valueOf(70_000)).build(),
                        GroupExpenseSplitRequest.builder().memberId(2L).amount(BigDecimal.valueOf(30_000)).build()
                ))
                .expenseDate(LocalDate.of(2026, 10, 18)).build();

        expenseService.createExpense(10L, 20L, request);

        ArgumentCaptor<GroupExpenseSplit> captor = ArgumentCaptor.forClass(GroupExpenseSplit.class);
        verify(splitRepository, times(2)).save(captor.capture());
        assertEquals(List.of(BigDecimal.valueOf(70_000), BigDecimal.valueOf(30_000)),
                captor.getAllValues().stream().map(GroupExpenseSplit::getSplitAmount).toList());
    }

    @Test
    void rejectsCustomSplitsWhenAmountsDoNotMatchExpenseTotal() {
        ExpenseGroup group = ExpenseGroup.builder().id(10L).build();
        GroupMember payer = GroupMember.builder().id(1L).group(group).name("An").build();
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(memberRepository.findByIdAndGroup_Id(1L, 10L)).thenReturn(Optional.of(payer));

        CreateGroupExpenseRequest request = CreateGroupExpenseRequest.builder()
                .name("Hotel").amount(BigDecimal.valueOf(100_000)).paidByMemberId(1L)
                .splitMethod("CUSTOM")
                .splitDetails(List.of(
                        GroupExpenseSplitRequest.builder().memberId(1L).amount(BigDecimal.valueOf(90_000)).build()
                ))
                .expenseDate(LocalDate.of(2026, 10, 18)).build();

        assertThrows(IllegalArgumentException.class, () -> expenseService.createExpense(10L, 20L, request));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void createsPercentageSplitsAndAssignsRoundingRemainderToLastMember() {
        ExpenseGroup group = ExpenseGroup.builder().id(10L).build();
        GroupMember first = GroupMember.builder().id(1L).group(group).name("An").build();
        GroupMember second = GroupMember.builder().id(2L).group(group).name("Binh").build();
        GroupMember third = GroupMember.builder().id(3L).group(group).name("Chi").build();
        GroupExpense savedExpense = GroupExpense.builder()
                .id(32L).group(group).name("Taxi").amount(BigDecimal.valueOf(101))
                .paidBy(first).expenseDate(LocalDate.of(2026, 10, 18)).build();
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(memberRepository.findByIdAndGroup_Id(1L, 10L)).thenReturn(Optional.of(first));
        when(memberRepository.findByIdAndGroup_Id(2L, 10L)).thenReturn(Optional.of(second));
        when(memberRepository.findByIdAndGroup_Id(3L, 10L)).thenReturn(Optional.of(third));
        when(expenseRepository.save(any(GroupExpense.class))).thenReturn(savedExpense);
        when(splitRepository.findByExpense_Id(32L)).thenReturn(List.of());

        CreateGroupExpenseRequest request = CreateGroupExpenseRequest.builder()
                .name("Taxi").amount(BigDecimal.valueOf(101)).paidByMemberId(1L)
                .splitMethod("PERCENTAGE")
                .splitDetails(List.of(
                        GroupExpenseSplitRequest.builder().memberId(1L).percentage(new BigDecimal("33.33")).build(),
                        GroupExpenseSplitRequest.builder().memberId(2L).percentage(new BigDecimal("33.33")).build(),
                        GroupExpenseSplitRequest.builder().memberId(3L).percentage(new BigDecimal("33.34")).build()
                ))
                .expenseDate(LocalDate.of(2026, 10, 18)).build();

        expenseService.createExpense(10L, 20L, request);

        ArgumentCaptor<GroupExpenseSplit> captor = ArgumentCaptor.forClass(GroupExpenseSplit.class);
        verify(splitRepository, times(3)).save(captor.capture());
        assertEquals(List.of(BigDecimal.valueOf(33), BigDecimal.valueOf(33), BigDecimal.valueOf(35)),
                captor.getAllValues().stream().map(GroupExpenseSplit::getSplitAmount).toList());
    }
}
