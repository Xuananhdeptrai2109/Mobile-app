package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.AddGroupMemberRequest;
import com.dev.expensemanagement.dto.request.CreateGroupTaskRequest;
import com.dev.expensemanagement.dto.response.GroupMemberResponse;
import com.dev.expensemanagement.entity.ExpenseGroup;
import com.dev.expensemanagement.entity.GroupMember;
import com.dev.expensemanagement.dto.request.UpdateGroupRequest;
import com.dev.expensemanagement.repository.ExpenseGroupRepository;
import com.dev.expensemanagement.repository.GroupMemberRepository;
import com.dev.expensemanagement.repository.GroupExpenseRepository;
import com.dev.expensemanagement.repository.GroupExpenseSplitRepository;
import com.dev.expensemanagement.repository.GroupTaskRepository;
import com.dev.expensemanagement.repository.UserRepository;
import com.dev.expensemanagement.security.GroupAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

class GroupServiceScopeTest {

    private ExpenseGroupRepository groupRepository;
    private GroupMemberRepository memberRepository;
    private GroupTaskRepository taskRepository;
    private GroupExpenseRepository expenseRepository;
    private GroupExpenseSplitRepository splitRepository;
    private GroupAuthorizationService authorizationService;
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupRepository = mock(ExpenseGroupRepository.class);
        memberRepository = mock(GroupMemberRepository.class);
        taskRepository = mock(GroupTaskRepository.class);
        expenseRepository = mock(GroupExpenseRepository.class);
        splitRepository = mock(GroupExpenseSplitRepository.class);
        authorizationService = mock(GroupAuthorizationService.class);
        groupService = new GroupService(
                groupRepository,
                memberRepository,
                taskRepository,
                expenseRepository,
                splitRepository,
                mock(UserRepository.class),
                authorizationService,
                mock(GroupActivityService.class)
        );
    }

    @Test
    void toggleLooksUpTaskInsideRequestedGroup() {
        when(taskRepository.findByIdAndGroup_Id(30L, 10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> groupService.toggleTask(10L, 30L, 20L));

        verify(authorizationService).requireMember(10L, 20L);
        verify(taskRepository).findByIdAndGroup_Id(30L, 10L);
    }

    @Test
    void createRejectsAssigneeFromAnotherGroup() {
        when(groupRepository.findById(10L)).thenReturn(Optional.of(ExpenseGroup.builder().id(10L).build()));
        when(memberRepository.findByIdAndGroup_Id(99L, 10L)).thenReturn(Optional.empty());
        CreateGroupTaskRequest request = CreateGroupTaskRequest.builder()
                .title("Book hotel")
                .assigneeMemberId(99L)
                .dueDate(LocalDate.of(2026, 10, 8))
                .build();

        assertThrows(IllegalArgumentException.class, () -> groupService.createTask(10L, 20L, request));

        verify(authorizationService).requireMember(10L, 20L);
        verify(memberRepository).findByIdAndGroup_Id(99L, 10L);
    }

    @Test
    void deleteRequiresOwnerAndUsesGroupScopedLookup() {
        when(taskRepository.findByIdAndGroup_Id(30L, 10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> groupService.deleteTask(10L, 30L, 20L));

        verify(authorizationService).requireOwner(10L, 20L);
        verify(taskRepository).findByIdAndGroup_Id(30L, 10L);
    }

    @Test
    void updateGroupRequiresOwnerAndUsesRequestedGroup() {
        when(groupRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> groupService.updateGroup(
                10L,
                20L,
                UpdateGroupRequest.builder().name("Trip").build()
        ));

        verify(authorizationService).requireOwner(10L, 20L);
        verify(groupRepository).findById(10L);
    }

    @Test
    void removeMemberPreservesExpenseHistory() {
        GroupMember owner = GroupMember.builder().id(1L).role("OWNER").build();
        GroupMember member = GroupMember.builder().id(2L).role("MEMBER").build();
        when(authorizationService.requireOwner(10L, 20L)).thenReturn(owner);
        when(memberRepository.findByIdAndGroup_Id(2L, 10L)).thenReturn(Optional.of(member));
        when(expenseRepository.existsByPaidBy_Id(2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> groupService.removeMember(10L, 2L, 20L));

        verify(memberRepository, never()).delete(member);
    }

    @Test
    void addMemberCreatesNameOnlyGuestWithoutUserAccount() {
        ExpenseGroup group = ExpenseGroup.builder().id(10L).build();
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(memberRepository.existsByGroupAndName(group, "Lan")).thenReturn(false);
        when(memberRepository.save(any(GroupMember.class))).thenAnswer(invocation -> {
            GroupMember member = invocation.getArgument(0);
            member.setId(2L);
            return member;
        });

        GroupMemberResponse response = groupService.addMember(
                10L,
                20L,
                AddGroupMemberRequest.builder().name("  Lan  ").build()
        );

        assertEquals("Lan", response.getFullName());
        assertEquals("MEMBER", response.getRole());
        assertNull(response.getUserId());
        verify(authorizationService).requireOwner(10L, 20L);
    }
}
