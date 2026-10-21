package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.response.GroupActivityResponse;
import com.dev.expensemanagement.entity.ExpenseGroup;
import com.dev.expensemanagement.entity.GroupActivity;
import com.dev.expensemanagement.entity.User;
import com.dev.expensemanagement.repository.GroupActivityRepository;
import com.dev.expensemanagement.repository.GroupMemberRepository;
import com.dev.expensemanagement.repository.UserRepository;
import com.dev.expensemanagement.security.GroupAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupActivityService {
    private final GroupActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository memberRepository;
    private final GroupAuthorizationService authorizationService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<GroupActivityResponse> getActivities(Long groupId, Long currentUserId) {
        authorizationService.requireMember(groupId, currentUserId);
        return activityRepository.findByGroup_IdOrderByCreatedAtDesc(groupId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void record(
            ExpenseGroup group,
            Long actorUserId,
            String type,
            String title,
            String description,
            String relatedEntityType,
            Long relatedEntityId,
            boolean notifyMembers
    ) {
        User actor = actorUserId == null ? null : userRepository.findById(actorUserId).orElse(null);
        activityRepository.save(GroupActivity.builder()
                .group(group)
                .actor(actor)
                .type(type)
                .title(title)
                .description(description)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .build());

        if (notifyMembers) {
            List<User> linkedUsers = memberRepository.findByGroup_Id(group.getId()).stream()
                    .map(member -> member.getUser())
                    .filter(java.util.Objects::nonNull)
                    .toList();
            notificationService.createNotifications(
                    linkedUsers,
                    group.getName() + ": " + title,
                    description
            );
        }
    }

    private GroupActivityResponse toResponse(GroupActivity activity) {
        return GroupActivityResponse.builder()
                .activityId(activity.getId())
                .groupId(activity.getGroup().getId())
                .actorName(activity.getActor() == null ? "Hệ thống" : activity.getActor().getFullName())
                .type(activity.getType())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .relatedEntityType(activity.getRelatedEntityType())
                .relatedEntityId(activity.getRelatedEntityId())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
