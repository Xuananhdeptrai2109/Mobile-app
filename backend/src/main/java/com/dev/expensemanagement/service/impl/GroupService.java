package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.AddGroupMemberRequest;
import com.dev.expensemanagement.dto.request.CreateGroupRequest;
import com.dev.expensemanagement.dto.request.CreateGroupTaskRequest;
import com.dev.expensemanagement.dto.request.UpdateGroupMemberRequest;
import com.dev.expensemanagement.dto.request.UpdateGroupRequest;
import com.dev.expensemanagement.dto.response.GroupMemberResponse;
import com.dev.expensemanagement.dto.response.GroupResponse;
import com.dev.expensemanagement.dto.response.GroupTaskResponse;
import com.dev.expensemanagement.entity.ExpenseGroup;
import com.dev.expensemanagement.entity.GroupMember;
import com.dev.expensemanagement.entity.GroupTask;
import com.dev.expensemanagement.entity.GroupExpense;
import com.dev.expensemanagement.entity.User;
import com.dev.expensemanagement.repository.ExpenseGroupRepository;
import com.dev.expensemanagement.repository.GroupMemberRepository;
import com.dev.expensemanagement.repository.GroupTaskRepository;
import com.dev.expensemanagement.repository.GroupExpenseRepository;
import com.dev.expensemanagement.repository.GroupExpenseSplitRepository;
import com.dev.expensemanagement.repository.UserRepository;
import com.dev.expensemanagement.security.GroupAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private static final String ROLE_OWNER = "OWNER";
    private static final String ROLE_MEMBER = "MEMBER";

    private final ExpenseGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupTaskRepository groupTaskRepository;
    private final GroupExpenseRepository groupExpenseRepository;
    private final GroupExpenseSplitRepository groupExpenseSplitRepository;
    private final UserRepository userRepository;
    private final GroupAuthorizationService authorizationService;
    private final GroupActivityService activityService;

    @Transactional
    public GroupResponse createGroup(Long currentUserId, CreateGroupRequest request) {
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Nguoi tao nhom khong ton tai"));

        List<String> memberNames = request.getMemberNames() == null
                ? List.of()
                : request.getMemberNames().stream().map(String::trim).toList();
        Set<String> uniqueMemberNames = memberNames.stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        if (uniqueMemberNames.size() != memberNames.size()) {
            throw new IllegalArgumentException("Ten thanh vien ban dau khong duoc trung nhau");
        }
        if (memberNames.stream().anyMatch(name -> name.equalsIgnoreCase(creator.getFullName()))) {
            throw new IllegalArgumentException("Thanh vien ban dau khong duoc trung ten chu nhom");
        }

        ExpenseGroup group = ExpenseGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageBase64(request.getImageBase64())
                .budget(request.getBudget())
                .createdBy(creator)
                .build();

        ExpenseGroup savedGroup = groupRepository.save(group);

        GroupMember ownerMember = GroupMember.builder()
                .group(savedGroup)
                .user(creator)
                .name(creator.getFullName())
                .role(ROLE_OWNER)
                .build();
        groupMemberRepository.save(ownerMember);

        List<GroupMember> guestMembers = memberNames.stream()
                .map(memberName -> GroupMember.builder()
                        .group(savedGroup)
                        .name(memberName)
                        .role(ROLE_MEMBER)
                        .build())
                .toList();
        groupMemberRepository.saveAll(guestMembers);

        activityService.record(
                savedGroup, currentUserId, "GROUP_CREATED", "Đã tạo nhóm",
                savedGroup.getName(), "GROUP", savedGroup.getId(), false
        );

        return toGroupResponse(savedGroup, "Tao nhom thanh cong", true, true);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(Long currentUserId) {
        return groupMemberRepository.findByUser_Id(currentUserId).stream()
                .map(member -> toGroupResponse(member.getGroup(), "Lay danh sach nhom thanh cong", true, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long groupId, Long currentUserId) {
        authorizationService.requireMember(groupId, currentUserId);
        ExpenseGroup group = findGroup(groupId);
        return toGroupResponse(group, "Lay thong tin nhom thanh cong", true, true);
    }

    @Transactional
    public GroupResponse updateGroup(Long groupId, Long currentUserId, UpdateGroupRequest request) {
        authorizationService.requireOwner(groupId, currentUserId);
        ExpenseGroup group = findGroup(groupId);

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isBlank()) {
                throw new IllegalArgumentException("Ten nhom khong duoc trong");
            }
            group.setName(name);
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription().trim());
        }
        if (request.getImageBase64() != null) {
            group.setImageBase64(request.getImageBase64());
        }
        if (request.getBudget() != null) {
            group.setBudget(request.getBudget());
        }

        ExpenseGroup saved = groupRepository.save(group);
        activityService.record(saved, currentUserId, "GROUP_UPDATED", "Đã cập nhật nhóm",
                saved.getName(), "GROUP", saved.getId(), false);
        return toGroupResponse(saved, "Cap nhat nhom thanh cong", true, true);
    }

    @Transactional
    public void deleteGroup(Long groupId, Long currentUserId) {
        authorizationService.requireOwner(groupId, currentUserId);
        ExpenseGroup group = findGroup(groupId);

        List<GroupExpense> expenses = groupExpenseRepository
                .findByGroup_IdOrderByExpenseDateDescCreatedAtDesc(groupId);
        expenses.forEach(expense ->
                groupExpenseSplitRepository.deleteAll(groupExpenseSplitRepository.findByExpense_Id(expense.getId()))
        );
        groupExpenseSplitRepository.flush();
        groupExpenseRepository.deleteAll(expenses);
        groupExpenseRepository.flush();
        groupTaskRepository.deleteAll(groupTaskRepository.findByGroup_Id(groupId));
        groupTaskRepository.flush();
        groupMemberRepository.deleteAll(groupMemberRepository.findByGroup_Id(groupId));
        groupMemberRepository.flush();
        groupRepository.delete(group);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getMembers(Long groupId, Long currentUserId) {
        authorizationService.requireMember(groupId, currentUserId);
        findGroup(groupId);
        return groupMemberRepository.findByGroup_Id(groupId).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public GroupMemberResponse addMember(Long groupId, Long currentUserId, AddGroupMemberRequest request) {
        authorizationService.requireOwner(groupId, currentUserId);
        ExpenseGroup group = findGroup(groupId);
        
        String memberName = request.getName().trim();

        if (groupMemberRepository.existsByGroupAndName(group, memberName)) {
            throw new IllegalArgumentException("Ten thanh vien da ton tai trong nhom");
        }

        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .user(null)
                .name(memberName)
                .role(ROLE_MEMBER)
                .build();

        GroupMember saved = groupMemberRepository.save(groupMember);
        activityService.record(group, currentUserId, "MEMBER_ADDED", "Đã thêm thành viên",
                saved.getName(), "MEMBER", saved.getId(), false);
        return toMemberResponse(saved);
    }

    @Transactional
    public GroupMemberResponse updateMember(
            Long groupId,
            Long memberId,
            Long currentUserId,
            UpdateGroupMemberRequest request
    ) {
        authorizationService.requireOwner(groupId, currentUserId);
        GroupMember member = findMember(groupId, memberId);
        if (member.getUser() != null) {
            throw new IllegalArgumentException("Khong the doi ten thanh vien da lien ket tai khoan");
        }

        String name = request.getName().trim();
        if (groupMemberRepository.existsByGroupAndName(member.getGroup(), name)
                && !member.getName().equalsIgnoreCase(name)) {
            throw new IllegalArgumentException("Ten thanh vien da ton tai trong nhom");
        }
        String oldName = member.getName();
        member.setName(name);
        GroupMember saved = groupMemberRepository.save(member);
        activityService.record(saved.getGroup(), currentUserId, "MEMBER_UPDATED", "Đã đổi tên thành viên",
                oldName + " → " + saved.getName(), "MEMBER", saved.getId(), false);
        return toMemberResponse(saved);
    }

    @Transactional
    public void removeMember(Long groupId, Long memberId, Long currentUserId) {
        GroupMember currentOwner = authorizationService.requireOwner(groupId, currentUserId);
        GroupMember member = findMember(groupId, memberId);
        if (currentOwner.getId().equals(member.getId()) || ROLE_OWNER.equals(member.getRole())) {
            throw new IllegalArgumentException("Khong the xoa chu nhom");
        }
        String memberName = member.getName();
        ExpenseGroup group = member.getGroup();
        removeMemberIfUnused(groupId, member);
        activityService.record(group, currentUserId, "MEMBER_REMOVED", "Đã xóa thành viên",
                memberName, "MEMBER", memberId, false);
    }

    @Transactional
    public GroupTaskResponse createTask(Long groupId, Long currentUserId, CreateGroupTaskRequest request) {
        authorizationService.requireMember(groupId, currentUserId);
        ExpenseGroup group = findGroup(groupId);
        GroupMember assignee = findOptionalMemberInGroup(groupId, request.getAssigneeMemberId());

        GroupTask task = GroupTask.builder()
                .group(group)
                .title(request.getTitle())
                .assignee(assignee)
                .dueDate(request.getDueDate())
                .description(request.getDescription())
                .isCompleted(false)
                .build();

        GroupTask saved = groupTaskRepository.save(task);
        activityService.record(
                group, currentUserId, "TASK_CREATED", "Nhiệm vụ mới", saved.getTitle(),
                "TASK", saved.getId(), true
        );
        return toTaskResponse(saved, "Tao nhiem vu thanh cong", true);
    }

    @Transactional(readOnly = true)
    public List<GroupTaskResponse> getGroupTasks(Long groupId, Long currentUserId) {
        authorizationService.requireMember(groupId, currentUserId);
        findGroup(groupId);
        return groupTaskRepository.findByGroup_Id(groupId).stream()
                .map(t -> toTaskResponse(t, "Lay danh sach nhiem vu thanh cong", true))
                .toList();
    }

    @Transactional
    public GroupTaskResponse updateTask(Long groupId, Long taskId, Long currentUserId, CreateGroupTaskRequest request) {
        authorizationService.requireMember(groupId, currentUserId);
        GroupTask task = groupTaskRepository.findByIdAndGroup_Id(taskId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Nhiem vu khong ton tai"));

        GroupMember assignee = findOptionalMemberInGroup(groupId, request.getAssigneeMemberId());

        task.setTitle(request.getTitle());
        task.setAssignee(assignee);
        task.setDueDate(request.getDueDate());
        task.setDescription(request.getDescription());

        GroupTask saved = groupTaskRepository.save(task);
        activityService.record(saved.getGroup(), currentUserId, "TASK_UPDATED", "Đã cập nhật nhiệm vụ",
                saved.getTitle(), "TASK", saved.getId(), false);
        return toTaskResponse(saved, "Cap nhat nhiem vu thanh cong", true);
    }

    @Transactional
    public GroupTaskResponse toggleTask(Long groupId, Long taskId, Long currentUserId) {
        authorizationService.requireMember(groupId, currentUserId);
        GroupTask task = groupTaskRepository.findByIdAndGroup_Id(taskId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Nhiem vu khong ton tai"));

        task.setCompleted(!task.isCompleted());
        GroupTask saved = groupTaskRepository.save(task);
        activityService.record(saved.getGroup(), currentUserId, "TASK_STATUS_UPDATED",
                saved.isCompleted() ? "Đã hoàn thành nhiệm vụ" : "Đã mở lại nhiệm vụ",
                saved.getTitle(), "TASK", saved.getId(), false);
        return toTaskResponse(saved, "Cap nhat trang thai thanh cong", true);
    }

    @Transactional
    public void deleteTask(Long groupId, Long taskId, Long currentUserId) {
        authorizationService.requireOwner(groupId, currentUserId);
        GroupTask task = groupTaskRepository.findByIdAndGroup_Id(taskId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Nhiem vu khong ton tai"));
        groupTaskRepository.delete(task);
        activityService.record(task.getGroup(), currentUserId, "TASK_DELETED", "Đã xóa nhiệm vụ",
                task.getTitle(), "TASK", taskId, false);
    }

    private GroupMember findOptionalMemberInGroup(Long groupId, Long memberId) {
        if (memberId == null) {
            return null;
        }
        return groupMemberRepository.findByIdAndGroup_Id(memberId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Thanh vien khong thuoc nhom nay"));
    }

    private GroupMember findMember(Long groupId, Long memberId) {
        return groupMemberRepository.findByIdAndGroup_Id(memberId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Thanh vien khong thuoc nhom nay"));
    }

    private void removeMemberIfUnused(Long groupId, GroupMember member) {
        if (groupExpenseRepository.existsByPaidBy_Id(member.getId())
                || groupExpenseSplitRepository.existsByMember_Id(member.getId())) {
            throw new IllegalArgumentException("Khong the xoa thanh vien da co lich su chi tieu");
        }

        List<GroupTask> affectedTasks = groupTaskRepository.findByGroup_Id(groupId).stream()
                .filter(task -> task.getAssignee() != null && task.getAssignee().getId().equals(member.getId()))
                .peek(task -> task.setAssignee(null))
                .toList();
        groupTaskRepository.saveAll(affectedTasks);
        groupTaskRepository.flush();
        groupMemberRepository.delete(member);
    }

    private ExpenseGroup findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Nhom khong ton tai"));
    }

    private GroupResponse toGroupResponse(ExpenseGroup group, String message, boolean success, boolean includeImage) {
        List<GroupMemberResponse> members = groupMemberRepository.findByGroup_Id(group.getId()).stream()
                .map(this::toMemberResponse)
                .toList();

        return GroupResponse.builder()
                .groupId(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .imageBase64(includeImage ? group.getImageBase64() : null)
                .budget(group.getBudget())
                .createdByUserId(group.getCreatedBy().getId())
                .createdByFullName(group.getCreatedBy().getFullName())
                .members(members)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .message(message)
                .success(success)
                .build();
    }

    private GroupMemberResponse toMemberResponse(GroupMember member) {
        User user = member.getUser();
        return GroupMemberResponse.builder()
                .memberId(member.getId())
                .userId(user != null ? user.getId() : null)
                .fullName(member.getName())
                .email(user != null ? user.getEmail() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    private GroupTaskResponse toTaskResponse(GroupTask task, String message, boolean success) {
        GroupMember assignee = task.getAssignee();
        return GroupTaskResponse.builder()
                .taskId(task.getId())
                .groupId(task.getGroup().getId())
                .title(task.getTitle())
                .assigneeMemberId(assignee != null ? assignee.getId() : null)
                .assigneeName(assignee != null ? assignee.getName() : "Chua phan cong")
                .dueDate(task.getDueDate())
                .description(task.getDescription())
                .isCompleted(task.isCompleted())
                .message(message)
                .success(success)
                .build();
    }
}
