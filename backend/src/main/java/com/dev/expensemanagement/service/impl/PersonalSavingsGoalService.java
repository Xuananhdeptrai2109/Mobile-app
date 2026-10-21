package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.*;
import com.dev.expensemanagement.dto.response.SavingsGoalResponse;
import com.dev.expensemanagement.entity.*;
import com.dev.expensemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalSavingsGoalService {
    private final PersonalSavingsGoalRepository goalRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getGoals(Long userId, boolean includeCompleted) {
        List<PersonalSavingsGoal> goals = includeCompleted
                ? goalRepository.findByUser_IdOrderByStatusAscCreatedAtDesc(userId)
                : goalRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, SavingsGoalStatus.ACTIVE);
        return goals.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SavingsGoalResponse getGoal(Long userId, Long goalId) {
        return toResponse(findOwned(userId, goalId));
    }

    @Transactional
    public SavingsGoalResponse createGoal(Long userId, SavingsGoalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        PersonalSavingsGoal goal = PersonalSavingsGoal.builder()
                .user(user)
                .title(request.getTitle().trim())
                .emoji(normalizeEmoji(request.getEmoji()))
                .targetAmount(request.getTargetAmount())
                .currentAmount(BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .status(SavingsGoalStatus.ACTIVE)
                .build();
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public SavingsGoalResponse updateGoal(Long userId, Long goalId, SavingsGoalRequest request) {
        PersonalSavingsGoal goal = goalRepository.findOwnedForUpdate(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Mục tiêu không tồn tại"));
        goal.setTitle(request.getTitle().trim());
        goal.setEmoji(normalizeEmoji(request.getEmoji()));
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        updateStatus(goal);
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public SavingsGoalResponse contribute(Long userId, Long goalId, SavingsContributionRequest request) {
        PersonalSavingsGoal goal = goalRepository.findOwnedForUpdate(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Mục tiêu không tồn tại"));
        BigDecimal updated = request.getOperation() == SavingsOperation.DEPOSIT
                ? goal.getCurrentAmount().add(request.getAmount())
                : goal.getCurrentAmount().subtract(request.getAmount());
        if (updated.signum() < 0) {
            throw new IllegalArgumentException("Số tiền rút không được vượt quá số tiền đã tích lũy");
        }
        goal.setCurrentAmount(updated);
        updateStatus(goal);
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        goalRepository.delete(findOwned(userId, goalId));
    }

    private PersonalSavingsGoal findOwned(Long userId, Long goalId) {
        return goalRepository.findByIdAndUser_Id(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Mục tiêu không tồn tại"));
    }

    private void updateStatus(PersonalSavingsGoal goal) {
        goal.setStatus(goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0
                ? SavingsGoalStatus.COMPLETED
                : SavingsGoalStatus.ACTIVE);
    }

    private String normalizeEmoji(String emoji) {
        return emoji == null || emoji.isBlank() ? "🎯" : emoji.trim();
    }

    private SavingsGoalResponse toResponse(PersonalSavingsGoal goal) {
        BigDecimal progress = goal.getTargetAmount().signum() == 0
                ? BigDecimal.ZERO
                : goal.getCurrentAmount().multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP)
                    .min(BigDecimal.valueOf(100));
        return SavingsGoalResponse.builder()
                .goalId(goal.getId())
                .title(goal.getTitle())
                .emoji(goal.getEmoji())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .progressPercentage(progress)
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
