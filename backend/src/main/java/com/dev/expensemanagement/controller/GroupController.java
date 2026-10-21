package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.dto.request.AddGroupMemberRequest;
import com.dev.expensemanagement.dto.request.CreateGroupRequest;
import com.dev.expensemanagement.dto.request.CreateGroupTaskRequest;
import com.dev.expensemanagement.dto.request.UpdateGroupMemberRequest;
import com.dev.expensemanagement.dto.request.UpdateGroupRequest;
import com.dev.expensemanagement.dto.response.GroupMemberResponse;
import com.dev.expensemanagement.dto.response.GroupResponse;
import com.dev.expensemanagement.dto.response.GroupTaskResponse;
import com.dev.expensemanagement.service.GroupService;
import com.dev.expensemanagement.security.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Validated
public class GroupController {

    private final GroupService groupService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<?> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        try {
            GroupResponse response = groupService.createGroup(currentUserService.requireUserId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<GroupResponse>> getUserGroups() {
        return ResponseEntity.ok(groupService.getUserGroups(currentUserService.requireUserId()));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable Long groupId) {
        try {
            return ResponseEntity.ok(groupService.getGroup(groupId, currentUserService.requireUserId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(e.getMessage()));
        }
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        try {
            return ResponseEntity.ok(groupService.updateGroup(
                    groupId,
                    currentUserService.requireUserId(),
                    request
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId) {
        try {
            groupService.deleteGroup(groupId, currentUserService.requireUserId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Xoa nhom thanh cong"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long groupId) {
        try {
            List<GroupMemberResponse> members = groupService.getMembers(groupId, currentUserService.requireUserId());
            return ResponseEntity.ok(members);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(e.getMessage()));
        }
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMember(
            @PathVariable Long groupId,
            @Valid @RequestBody AddGroupMemberRequest request
    ) {
        try {
            GroupMemberResponse response = groupService.addMember(groupId, currentUserService.requireUserId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PatchMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> updateMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateGroupMemberRequest request
    ) {
        try {
            return ResponseEntity.ok(groupService.updateMember(
                    groupId,
                    memberId,
                    currentUserService.requireUserId(),
                    request
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId
    ) {
        try {
            groupService.removeMember(groupId, memberId, currentUserService.requireUserId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Xoa thanh vien thanh cong"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PostMapping("/{groupId}/tasks")
    public ResponseEntity<?> createTask(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupTaskRequest request
    ) {
        try {
            GroupTaskResponse response = groupService.createTask(groupId, currentUserService.requireUserId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @GetMapping("/{groupId}/tasks")
    public ResponseEntity<?> getTasks(@PathVariable Long groupId) {
        try {
            List<GroupTaskResponse> tasks = groupService.getGroupTasks(groupId, currentUserService.requireUserId());
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(e.getMessage()));
        }
    }

    @PutMapping("/{groupId}/tasks/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            @Valid @RequestBody CreateGroupTaskRequest request
    ) {
        try {
            GroupTaskResponse response = groupService.updateTask(groupId, taskId, currentUserService.requireUserId(), request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PatchMapping("/{groupId}/tasks/{taskId}/toggle")
    public ResponseEntity<?> toggleTask(
            @PathVariable Long groupId,
            @PathVariable Long taskId
    ) {
        try {
            GroupTaskResponse response = groupService.toggleTask(groupId, taskId, currentUserService.requireUserId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(
            @PathVariable Long groupId,
            @PathVariable Long taskId
    ) {
        try {
            groupService.deleteTask(groupId, taskId, currentUserService.requireUserId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Xoa nhiem vu thanh cong"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    private Map<String, Object> error(String message) {
        return Map.of(
                "success", false,
                "message", message
        );
    }
}
