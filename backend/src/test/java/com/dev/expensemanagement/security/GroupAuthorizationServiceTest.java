package com.dev.expensemanagement.security;

import com.dev.expensemanagement.entity.GroupMember;
import com.dev.expensemanagement.repository.GroupMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupAuthorizationServiceTest {

    private GroupMemberRepository memberRepository;
    private GroupAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(GroupMemberRepository.class);
        authorizationService = new GroupAuthorizationService(memberRepository);
    }

    @Test
    void memberCanAccessItsOwnGroup() {
        GroupMember member = GroupMember.builder().id(7L).role("MEMBER").build();
        when(memberRepository.findByGroup_IdAndUser_Id(10L, 20L)).thenReturn(Optional.of(member));

        assertEquals(member, authorizationService.requireMember(10L, 20L));
    }

    @Test
    void nonMemberIsRejected() {
        when(memberRepository.findByGroup_IdAndUser_Id(10L, 20L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> authorizationService.requireMember(10L, 20L));
    }

    @Test
    void regularMemberCannotPerformOwnerAction() {
        GroupMember member = GroupMember.builder().role("MEMBER").build();
        when(memberRepository.findByGroup_IdAndUser_Id(10L, 20L)).thenReturn(Optional.of(member));

        assertThrows(AccessDeniedException.class, () -> authorizationService.requireOwner(10L, 20L));
    }
}
