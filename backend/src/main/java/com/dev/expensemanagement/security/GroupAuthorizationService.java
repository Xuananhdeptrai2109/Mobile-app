package com.dev.expensemanagement.security;

import com.dev.expensemanagement.entity.GroupMember;
import com.dev.expensemanagement.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupAuthorizationService {

    private static final String ROLE_OWNER = "OWNER";

    private final GroupMemberRepository groupMemberRepository;

    public GroupMember requireMember(Long groupId, Long userId) {
        return groupMemberRepository.findByGroup_IdAndUser_Id(groupId, userId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên của nhóm này"));
    }

    /** Serializes operations that must be unique for one user inside one group. */
    public GroupMember requireMemberForUpdate(Long groupId, Long userId) {
        return groupMemberRepository.findByGroupIdAndUserIdForUpdate(groupId, userId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên của nhóm này"));
    }

    public GroupMember requireOwner(Long groupId, Long userId) {
        GroupMember member = requireMember(groupId, userId);
        if (!ROLE_OWNER.equals(member.getRole())) {
            throw new AccessDeniedException("Chỉ chủ nhóm mới được thực hiện thao tác này");
        }
        return member;
    }
}
