package com.dev.expensemanagement.service;

import com.dev.expensemanagement.entity.ExpenseGroup;
import com.dev.expensemanagement.entity.GroupExpense;
import com.dev.expensemanagement.entity.GroupExpenseSplit;
import com.dev.expensemanagement.entity.GroupMember;
import com.dev.expensemanagement.entity.GroupTask;
import com.dev.expensemanagement.repository.ExpenseGroupRepository;
import com.dev.expensemanagement.repository.GroupExpenseRepository;
import com.dev.expensemanagement.repository.GroupExpenseSplitRepository;
import com.dev.expensemanagement.repository.GroupMemberRepository;
import com.dev.expensemanagement.repository.GroupTaskRepository;
import com.dev.expensemanagement.security.GroupAuthorizationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroupReportServiceTest {
    @Test
    void buildsExpenseTaskAndMemberTotals() {
        ExpenseGroupRepository groupRepository = mock(ExpenseGroupRepository.class);
        GroupMemberRepository memberRepository = mock(GroupMemberRepository.class);
        GroupExpenseRepository expenseRepository = mock(GroupExpenseRepository.class);
        GroupExpenseSplitRepository splitRepository = mock(GroupExpenseSplitRepository.class);
        GroupTaskRepository taskRepository = mock(GroupTaskRepository.class);
        GroupAuthorizationService authorizationService = mock(GroupAuthorizationService.class);
        GroupReportService service = new GroupReportService(
                groupRepository, memberRepository, expenseRepository, splitRepository,
                taskRepository, authorizationService
        );

        ExpenseGroup group = ExpenseGroup.builder().id(1L).name("Trip").budget(BigDecimal.valueOf(500)).build();
        GroupMember an = GroupMember.builder().id(10L).group(group).name("An").build();
        GroupMember binh = GroupMember.builder().id(11L).group(group).name("Binh").build();
        GroupExpense expense = GroupExpense.builder().id(20L).group(group).paidBy(an)
                .amount(BigDecimal.valueOf(300)).build();
        GroupExpenseSplit anSplit = GroupExpenseSplit.builder().expense(expense).member(an)
                .splitAmount(BigDecimal.valueOf(100)).isPaid(true).build();
        GroupExpenseSplit binhSplit = GroupExpenseSplit.builder().expense(expense).member(binh)
                .splitAmount(BigDecimal.valueOf(200)).isPaid(false).build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(memberRepository.findByGroup_Id(1L)).thenReturn(List.of(an, binh));
        when(expenseRepository.findByGroup_IdOrderByExpenseDateDescCreatedAtDesc(1L)).thenReturn(List.of(expense));
        when(splitRepository.findByExpense_Group_Id(1L)).thenReturn(List.of(anSplit, binhSplit));
        when(taskRepository.findByGroup_Id(1L)).thenReturn(List.of(
                GroupTask.builder().isCompleted(true).build(),
                GroupTask.builder().isCompleted(false).build()
        ));

        var report = service.getReport(1L, 99L);

        verify(authorizationService).requireMember(1L, 99L);
        assertEquals(BigDecimal.valueOf(300), report.getTotalExpense());
        assertEquals(BigDecimal.valueOf(100), report.getSettledAmount());
        assertEquals(BigDecimal.valueOf(200), report.getOutstandingAmount());
        assertEquals(1, report.getExpenseCount());
        assertEquals(1, report.getCompletedTaskCount());
        assertEquals(BigDecimal.valueOf(200), report.getMembers().get(0).getNetBalance());
        assertEquals(BigDecimal.valueOf(-200), report.getMembers().get(1).getNetBalance());
    }
}
