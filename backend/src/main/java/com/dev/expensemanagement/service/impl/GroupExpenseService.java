package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.CreateGroupExpenseRequest;
import com.dev.expensemanagement.dto.request.GroupExpenseSplitRequest;
import com.dev.expensemanagement.dto.response.GroupExpenseResponse;
import com.dev.expensemanagement.dto.response.GroupExpenseSplitResponse;
import com.dev.expensemanagement.entity.ExpenseGroup;
import com.dev.expensemanagement.entity.GroupExpense;
import com.dev.expensemanagement.entity.GroupExpenseSplit;
import com.dev.expensemanagement.entity.GroupExpenseSplitMethod;
import com.dev.expensemanagement.entity.GroupMember;
import com.dev.expensemanagement.repository.ExpenseGroupRepository;
import com.dev.expensemanagement.repository.GroupExpenseRepository;
import com.dev.expensemanagement.repository.GroupExpenseSplitRepository;
import com.dev.expensemanagement.repository.GroupMemberRepository;
import com.dev.expensemanagement.security.GroupAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GroupExpenseService {

    private final ExpenseGroupRepository groupRepository;
    private final GroupExpenseRepository expenseRepository;
    private final GroupExpenseSplitRepository splitRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupAuthorizationService authorizationService;
    private final GroupActivityService activityService;

    @Transactional
    public GroupExpenseResponse createExpense(Long groupId, Long currentUserId, CreateGroupExpenseRequest request) {
        authorizationService.requireMember(groupId, currentUserId);
        ExpenseGroup group = findGroup(groupId);
        GroupMember paidBy = findMember(groupId, request.getPaidByMemberId());

        GroupExpenseSplitMethod splitMethod = parseSplitMethod(request.getSplitMethod());
        List<ResolvedSplit> resolvedSplits = resolveSplits(groupId, request, splitMethod);

        GroupExpense expense = GroupExpense.builder()
                .group(group)
                .name(request.getName())
                .amount(request.getAmount())
                .paidBy(paidBy)
                .splitMethod(splitMethod)
                .note(request.getNote())
                .expenseDate(request.getExpenseDate())
                .receiptImageUrl(request.getReceiptImageUrl())
                .build();

        GroupExpense savedExpense = expenseRepository.save(expense);
        createSplits(savedExpense, resolvedSplits);
        activityService.record(
                group, currentUserId, "EXPENSE_CREATED", "Khoản chi mới",
                request.getName() + " - " + request.getAmount().toPlainString() + " VND",
                "EXPENSE", savedExpense.getId(), true
        );

        return toResponse(savedExpense, "Tao chi tieu thanh cong", true);
    }

    @Transactional(readOnly = true)
    public List<GroupExpenseResponse> getGroupExpenses(Long groupId, Long currentUserId) {
        authorizationService.requireMember(groupId, currentUserId);
        findGroup(groupId);
        return expenseRepository.findByGroup_IdOrderByExpenseDateDescCreatedAtDesc(groupId).stream()
                .map(expense -> toResponse(expense, "Lay chi tieu thanh cong", true))
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupExpenseResponse getExpense(Long groupId, Long expenseId, Long currentUserId) {
        authorizationService.requireMember(groupId, currentUserId);
        findGroup(groupId);
        GroupExpense expense = expenseRepository.findByIdAndGroup_Id(expenseId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Chi tieu khong ton tai"));

        return toResponse(expense, "Lay chi tieu thanh cong", true);
    }

    @Transactional
    public GroupExpenseResponse updateExpense(
            Long groupId,
            Long expenseId,
            Long currentUserId,
            CreateGroupExpenseRequest request
    ) {
        authorizationService.requireMember(groupId, currentUserId);
        GroupExpense expense = expenseRepository.findByIdAndGroup_Id(expenseId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Chi tieu khong ton tai"));
        GroupMember paidBy = findMember(groupId, request.getPaidByMemberId());
        GroupExpenseSplitMethod splitMethod = parseSplitMethod(request.getSplitMethod());
        List<ResolvedSplit> resolvedSplits = resolveSplits(groupId, request, splitMethod);

        expense.setName(request.getName());
        expense.setAmount(request.getAmount());
        expense.setPaidBy(paidBy);
        expense.setSplitMethod(splitMethod);
        expense.setNote(request.getNote());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setReceiptImageUrl(request.getReceiptImageUrl());

        splitRepository.deleteAll(splitRepository.findByExpense_Id(expenseId));
        splitRepository.flush();
        GroupExpense savedExpense = expenseRepository.save(expense);
        createSplits(savedExpense, resolvedSplits);
        activityService.record(
                savedExpense.getGroup(), currentUserId, "EXPENSE_UPDATED", "Đã cập nhật khoản chi",
                savedExpense.getName(), "EXPENSE", savedExpense.getId(), false
        );
        return toResponse(savedExpense, "Cap nhat chi tieu thanh cong", true);
    }

    @Transactional
    public void deleteExpense(Long groupId, Long expenseId, Long currentUserId) {
        authorizationService.requireMember(groupId, currentUserId);
        GroupExpense expense = expenseRepository.findByIdAndGroup_Id(expenseId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Chi tieu khong ton tai"));
        splitRepository.deleteAll(splitRepository.findByExpense_Id(expenseId));
        splitRepository.flush();
        expenseRepository.delete(expense);
        activityService.record(
                expense.getGroup(), currentUserId, "EXPENSE_DELETED", "Đã xóa khoản chi",
                expense.getName(), "EXPENSE", expenseId, false
        );
    }

    @Transactional
    public GroupExpenseSplitResponse toggleSplitPayment(
            Long groupId,
            Long expenseId,
            Long splitId,
            Long currentUserId
    ) {
        authorizationService.requireOwner(groupId, currentUserId);
        GroupExpenseSplit split = splitRepository
                .findByIdAndExpense_IdAndExpense_Group_Id(splitId, expenseId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Phan chia tien khong ton tai"));

        split.setPaid(!split.isPaid());
        GroupExpenseSplit saved = splitRepository.save(split);
        activityService.record(
                saved.getExpense().getGroup(), currentUserId, "PAYMENT_UPDATED",
                saved.isPaid() ? "Đã ghi nhận thanh toán" : "Đã hoàn tác thanh toán",
                saved.getMember().getName() + " - " + saved.getSplitAmount().toPlainString() + " VND",
                "EXPENSE", expenseId, true
        );
        return toSplitResponse(saved);
    }

    private void createSplits(GroupExpense expense, List<ResolvedSplit> splits) {
        for (ResolvedSplit split : splits) {
            splitRepository.save(GroupExpenseSplit.builder()
                    .expense(expense)
                    .member(split.member())
                    .splitAmount(split.amount())
                    .build());
        }
    }

    private List<ResolvedSplit> resolveSplits(
            Long groupId,
            CreateGroupExpenseRequest request,
            GroupExpenseSplitMethod method
    ) {
        if (method == GroupExpenseSplitMethod.EQUAL) {
            Set<Long> ids = new LinkedHashSet<>();
            if (request.getSplitMemberIds() != null) ids.addAll(request.getSplitMemberIds());
            if (ids.isEmpty() && request.getSplitDetails() != null) {
                request.getSplitDetails().forEach(detail -> ids.add(detail.getMemberId()));
            }
            if (ids.isEmpty()) throw new IllegalArgumentException("Phải chọn ít nhất một người để chia tiền");

            BigDecimal base = request.getAmount().divide(BigDecimal.valueOf(ids.size()), 0, RoundingMode.DOWN);
            BigDecimal assigned = BigDecimal.ZERO;
            List<ResolvedSplit> result = new ArrayList<>();
            int index = 0;
            for (Long memberId : ids) {
                BigDecimal value = index == ids.size() - 1 ? request.getAmount().subtract(assigned) : base;
                result.add(new ResolvedSplit(findMember(groupId, memberId), value));
                assigned = assigned.add(value);
                index++;
            }
            return result;
        }

        List<GroupExpenseSplitRequest> details = request.getSplitDetails();
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Phải nhập chi tiết chia tiền");
        }
        if (details.stream().anyMatch(detail -> detail == null || detail.getMemberId() == null)) {
            throw new IllegalArgumentException("Thành viên chia tiền không hợp lệ");
        }
        Set<Long> uniqueIds = new HashSet<>();
        if (details.stream().anyMatch(detail -> !uniqueIds.add(detail.getMemberId()))) {
            throw new IllegalArgumentException("Mỗi thành viên chỉ được xuất hiện một lần");
        }

        if (method == GroupExpenseSplitMethod.CUSTOM) {
            if (details.stream().anyMatch(detail -> detail.getAmount() == null
                    || detail.getAmount().compareTo(BigDecimal.ZERO) <= 0
                    || detail.getAmount().scale() > 0)) {
                throw new IllegalArgumentException("Số tiền tùy chỉnh phải là số nguyên dương");
            }
            BigDecimal sum = details.stream().map(GroupExpenseSplitRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(request.getAmount()) != 0) {
                throw new IllegalArgumentException("Tổng số tiền chia phải bằng tổng khoản chi");
            }
            return details.stream()
                    .map(detail -> new ResolvedSplit(findMember(groupId, detail.getMemberId()), detail.getAmount()))
                    .toList();
        }

        if (details.stream().anyMatch(detail -> detail.getPercentage() == null
                || detail.getPercentage().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Tỷ lệ của mỗi thành viên phải lớn hơn 0");
        }
        BigDecimal totalPercentage = details.stream().map(GroupExpenseSplitRequest::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalPercentage.compareTo(new BigDecimal("100")) != 0) {
            throw new IllegalArgumentException("Tổng tỷ lệ phải bằng 100%");
        }

        List<ResolvedSplit> result = new ArrayList<>();
        BigDecimal assigned = BigDecimal.ZERO;
        for (int index = 0; index < details.size(); index++) {
            GroupExpenseSplitRequest detail = details.get(index);
            BigDecimal value = index == details.size() - 1
                    ? request.getAmount().subtract(assigned)
                    : request.getAmount().multiply(detail.getPercentage())
                            .divide(new BigDecimal("100"), 0, RoundingMode.DOWN);
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Tỷ lệ tạo ra phần chia bằng 0 VND");
            }
            result.add(new ResolvedSplit(findMember(groupId, detail.getMemberId()), value));
            assigned = assigned.add(value);
        }
        return result;
    }

    private GroupExpenseSplitMethod parseSplitMethod(String value) {
        if (value == null || value.isBlank()) return GroupExpenseSplitMethod.EQUAL;
        try {
            return GroupExpenseSplitMethod.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            throw new IllegalArgumentException("Kiểu chia tiền không hợp lệ");
        }
    }

    private GroupMember findMember(Long groupId, Long memberId) {
        return groupMemberRepository.findByIdAndGroup_Id(memberId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Thanh vien khong thuoc nhom nay"));
    }

    private ExpenseGroup findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Nhom khong ton tai"));
    }

    private GroupExpenseResponse toResponse(GroupExpense expense, String message, boolean success) {
        List<GroupExpenseSplitResponse> splits = splitRepository.findByExpense_Id(expense.getId()).stream()
                .map(this::toSplitResponse)
                .toList();

        return GroupExpenseResponse.builder()
                .expenseId(expense.getId())
                .groupId(expense.getGroup().getId())
                .name(expense.getName())
                .amount(expense.getAmount())
                .paidByMemberId(expense.getPaidBy().getId())
                .paidByFullName(expense.getPaidBy().getName())
                .splitMethod((expense.getSplitMethod() == null
                        ? GroupExpenseSplitMethod.EQUAL : expense.getSplitMethod()).name())
                .note(expense.getNote())
                .expenseDate(expense.getExpenseDate())
                .receiptImageUrl(expense.getReceiptImageUrl())
                .splits(splits)
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .message(message)
                .success(success)
                .build();
    }

    private record ResolvedSplit(GroupMember member, BigDecimal amount) {}

    private GroupExpenseSplitResponse toSplitResponse(GroupExpenseSplit split) {
        return GroupExpenseSplitResponse.builder()
                .splitId(split.getId())
                .memberId(split.getMember().getId())
                .fullName(split.getMember().getName())
                .splitAmount(split.getSplitAmount())
                .isPaid(split.isPaid())
                .build();
    }
}
