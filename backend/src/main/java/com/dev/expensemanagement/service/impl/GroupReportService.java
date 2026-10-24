package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.response.GroupMemberReportResponse;
import com.dev.expensemanagement.dto.response.GroupReportResponse;
import com.dev.expensemanagement.entity.ExpenseGroup;
import com.dev.expensemanagement.entity.GroupExpense;
import com.dev.expensemanagement.entity.GroupExpenseSplit;
import com.dev.expensemanagement.entity.GroupMember;
import com.dev.expensemanagement.entity.GroupTask;
import com.dev.expensemanagement.entity.GroupSettlement;
import com.dev.expensemanagement.repository.ExpenseGroupRepository;
import com.dev.expensemanagement.repository.GroupExpenseRepository;
import com.dev.expensemanagement.repository.GroupExpenseSplitRepository;
import com.dev.expensemanagement.repository.GroupMemberRepository;
import com.dev.expensemanagement.repository.GroupTaskRepository;
import com.dev.expensemanagement.repository.GroupSettlementRepository;
import com.dev.expensemanagement.security.GroupAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroupReportService {
    private final ExpenseGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final GroupExpenseRepository expenseRepository;
    private final GroupExpenseSplitRepository splitRepository;
    private final GroupTaskRepository taskRepository;
    private final GroupSettlementRepository settlementRepository;
    private final GroupAuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public GroupReportResponse getReport(Long groupId, Long currentUserId) {
        authorizationService.requireMember(groupId, currentUserId);
        ExpenseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Nhóm không tồn tại"));
        List<GroupMember> members = memberRepository.findByGroup_Id(groupId);
        List<GroupExpense> expenses = expenseRepository.findByGroup_IdOrderByExpenseDateDescCreatedAtDesc(groupId);
        List<GroupTask> tasks = taskRepository.findByGroup_Id(groupId);

        List<GroupExpenseSplit> allSplits = splitRepository.findByExpense_Group_Id(groupId);
        List<GroupSettlement> settlements = settlementRepository.findByGroup_IdOrderBySettledAtDesc(groupId);

        BigDecimal totalExpense = expenses.stream()
                .map(GroupExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal settled = allSplits.stream()
                .filter(GroupExpenseSplit::isPaid)
                .filter(split -> !split.getMember().getId().equals(split.getExpense().getPaidBy().getId()))
                .map(GroupExpenseSplit::getSplitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(settlements.stream().map(GroupSettlement::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        Map<Long, BigDecimal> balances = new HashMap<>();
        members.forEach(member -> balances.put(member.getId(), BigDecimal.ZERO));
        allSplits.stream().filter(split -> !split.isPaid()).forEach(split -> {
            Long payerId = split.getExpense().getPaidBy().getId();
            Long memberId = split.getMember().getId();
            if (!payerId.equals(memberId)) {
                balances.merge(memberId, split.getSplitAmount().negate(), BigDecimal::add);
                balances.merge(payerId, split.getSplitAmount(), BigDecimal::add);
            }
        });
        settlements.forEach(item -> {
            balances.merge(item.getFromMember().getId(), item.getAmount(), BigDecimal::add);
            balances.merge(item.getToMember().getId(), item.getAmount().negate(), BigDecimal::add);
        });
        BigDecimal outstanding = balances.values().stream()
                .filter(balance -> balance.signum() < 0)
                .map(BigDecimal::negate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<GroupMemberReportResponse> memberReports = members.stream().map(member -> {
            BigDecimal paid = expenses.stream()
                    .filter(expense -> expense.getPaidBy().getId().equals(member.getId()))
                    .map(GroupExpense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            List<GroupExpenseSplit> memberSplits = allSplits.stream()
                    .filter(split -> split.getMember().getId().equals(member.getId())).toList();
            BigDecimal owed = memberSplits.stream().map(GroupExpenseSplit::getSplitAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal memberSettled = memberSplits.stream().filter(GroupExpenseSplit::isPaid)
                    .filter(split -> !split.getExpense().getPaidBy().getId().equals(member.getId()))
                    .map(GroupExpenseSplit::getSplitAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .add(settlements.stream()
                            .filter(item -> item.getFromMember().getId().equals(member.getId()))
                            .map(GroupSettlement::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
            BigDecimal currentBalance = balances.getOrDefault(member.getId(), BigDecimal.ZERO);
            return GroupMemberReportResponse.builder()
                    .memberId(member.getId())
                    .fullName(member.getName())
                    .totalPaid(paid)
                    .totalOwed(owed)
                    .settledAmount(memberSettled)
                    .outstandingAmount(currentBalance.signum() < 0 ? currentBalance.negate() : BigDecimal.ZERO)
                    .netBalance(currentBalance)
                    .build();
        }).toList();

        return GroupReportResponse.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .budget(group.getBudget())
                .totalExpense(totalExpense)
                .settledAmount(settled)
                .outstandingAmount(outstanding)
                .expenseCount(expenses.size())
                .taskCount(tasks.size())
                .completedTaskCount((int) tasks.stream().filter(GroupTask::isCompleted).count())
                .members(memberReports)
                .build();
    }

    @Transactional(readOnly = true)
    public String exportCsv(Long groupId, Long currentUserId) {
        GroupReportResponse report = getReport(groupId, currentUserId);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Báo cáo nhóm,").append(escape(report.getGroupName())).append("\n")
                .append("Tổng chi,").append(report.getTotalExpense().toPlainString()).append("\n")
                .append("Đã thanh toán,").append(report.getSettledAmount().toPlainString()).append("\n")
                .append("Còn phải thanh toán,").append(report.getOutstandingAmount().toPlainString()).append("\n\n")
                .append("Thành viên,Tổng đã ứng,Tổng phần chia,Đã thanh toán,Còn nợ,Số dư ròng\n");
        report.getMembers().forEach(member -> csv
                .append(escape(member.getFullName())).append(',')
                .append(member.getTotalPaid().toPlainString()).append(',')
                .append(member.getTotalOwed().toPlainString()).append(',')
                .append(member.getSettledAmount().toPlainString()).append(',')
                .append(member.getOutstandingAmount().toPlainString()).append(',')
                .append(member.getNetBalance().toPlainString()).append('\n'));
        return csv.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
