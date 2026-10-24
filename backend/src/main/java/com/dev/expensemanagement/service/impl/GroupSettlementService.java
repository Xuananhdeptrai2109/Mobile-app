package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.CreateGroupSettlementRequest;
import com.dev.expensemanagement.dto.response.GroupSettlementResponse;
import com.dev.expensemanagement.entity.GroupExpenseSplit;
import com.dev.expensemanagement.entity.GroupMember;
import com.dev.expensemanagement.entity.GroupSettlement;
import com.dev.expensemanagement.entity.User;
import com.dev.expensemanagement.repository.GroupExpenseSplitRepository;
import com.dev.expensemanagement.repository.GroupMemberRepository;
import com.dev.expensemanagement.repository.GroupSettlementRepository;
import com.dev.expensemanagement.repository.UserRepository;
import com.dev.expensemanagement.security.GroupAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroupSettlementService {
    private final GroupSettlementRepository settlementRepository;
    private final GroupMemberRepository memberRepository;
    private final GroupExpenseSplitRepository splitRepository;
    private final UserRepository userRepository;
    private final GroupAuthorizationService authorizationService;
    private final GroupActivityService activityService;

    @Transactional(readOnly = true)
    public List<GroupSettlementResponse> getSettlements(Long groupId, Long currentUserId) {
        authorizationService.requireMember(groupId, currentUserId);
        return settlementRepository.findByGroup_IdOrderBySettledAtDesc(groupId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GroupSettlementResponse settle(
            Long groupId,
            Long currentUserId,
            CreateGroupSettlementRequest request
    ) {
        GroupMember actor = authorizationService.requireMember(groupId, currentUserId);
        if (request.getFromMemberId().equals(request.getToMemberId())) {
            throw new IllegalArgumentException("Người trả và người nhận phải khác nhau");
        }

        GroupMember from = requireGroupMember(groupId, request.getFromMemberId());
        GroupMember to = requireGroupMember(groupId, request.getToMemberId());
        if (!actor.getId().equals(from.getId()) && !"OWNER".equals(actor.getRole())) {
            throw new AccessDeniedException("Bạn chỉ có thể xác nhận khoản nợ của chính mình");
        }
        Map<Long, BigDecimal> balances = calculateBalances(groupId);
        BigDecimal debt = balances.getOrDefault(from.getId(), BigDecimal.ZERO).negate();
        BigDecimal credit = balances.getOrDefault(to.getId(), BigDecimal.ZERO);
        BigDecimal maximum = debt.min(credit);
        if (maximum.signum() <= 0 || request.getAmount().compareTo(maximum) > 0) {
            throw new IllegalArgumentException("Khoản thanh toán vượt quá công nợ hiện tại");
        }

        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        GroupSettlement saved = settlementRepository.save(GroupSettlement.builder()
                .group(from.getGroup())
                .fromMember(from)
                .toMember(to)
                .amount(request.getAmount())
                .createdBy(creator)
                .build());
        activityService.record(
                from.getGroup(),
                currentUserId,
                "SETTLEMENT",
                "Đã ghi nhận thanh toán",
                from.getName() + " đã trả " + to.getName() + " " + request.getAmount().toPlainString() + " VND",
                "GROUP_SETTLEMENT",
                saved.getId(),
                true
        );
        return toResponse(saved);
    }

    private Map<Long, BigDecimal> calculateBalances(Long groupId) {
        Map<Long, BigDecimal> balances = new HashMap<>();
        memberRepository.findByGroup_Id(groupId)
                .forEach(member -> balances.put(member.getId(), BigDecimal.ZERO));

        for (GroupExpenseSplit split : splitRepository.findByExpense_Group_Id(groupId)) {
            if (split.isPaid()) continue;
            Long payerId = split.getExpense().getPaidBy().getId();
            Long beneficiaryId = split.getMember().getId();
            if (payerId.equals(beneficiaryId)) continue;
            balances.merge(beneficiaryId, split.getSplitAmount().negate(), BigDecimal::add);
            balances.merge(payerId, split.getSplitAmount(), BigDecimal::add);
        }

        for (GroupSettlement settlement : settlementRepository.findByGroup_IdOrderBySettledAtDesc(groupId)) {
            balances.merge(settlement.getFromMember().getId(), settlement.getAmount(), BigDecimal::add);
            balances.merge(settlement.getToMember().getId(), settlement.getAmount().negate(), BigDecimal::add);
        }
        return balances;
    }

    private GroupMember requireGroupMember(Long groupId, Long memberId) {
        return memberRepository.findByIdAndGroup_Id(memberId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Thành viên không thuộc nhóm"));
    }

    private GroupSettlementResponse toResponse(GroupSettlement settlement) {
        return GroupSettlementResponse.builder()
                .settlementId(settlement.getId())
                .groupId(settlement.getGroup().getId())
                .fromMemberId(settlement.getFromMember().getId())
                .fromMemberName(settlement.getFromMember().getName())
                .toMemberId(settlement.getToMember().getId())
                .toMemberName(settlement.getToMember().getName())
                .amount(settlement.getAmount())
                .settledAt(settlement.getSettledAt())
                .build();
    }
}
