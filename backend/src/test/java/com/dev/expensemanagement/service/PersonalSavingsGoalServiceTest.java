package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.*;
import com.dev.expensemanagement.entity.*;
import com.dev.expensemanagement.repository.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonalSavingsGoalServiceTest {
    private PersonalSavingsGoalRepository goalRepository;
    private UserRepository userRepository;
    private PersonalSavingsGoalService service;

    @BeforeEach
    void setUp() {
        goalRepository = mock(PersonalSavingsGoalRepository.class);
        userRepository = mock(UserRepository.class);
        service = new PersonalSavingsGoalService(goalRepository, userRepository);
    }

    @Test
    void depositCompletesGoalAtTarget() {
        PersonalSavingsGoal goal = goal(7L, 800_000, 1_000_000);
        when(goalRepository.findOwnedForUpdate(10L, 7L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(goal)).thenReturn(goal);

        var response = service.contribute(
                7L,
                10L,
                new SavingsContributionRequest(BigDecimal.valueOf(200_000), SavingsOperation.DEPOSIT)
        );

        assertEquals(BigDecimal.valueOf(1_000_000), goal.getCurrentAmount());
        assertEquals(SavingsGoalStatus.COMPLETED, response.getStatus());
        assertEquals(new BigDecimal("100.00"), response.getProgressPercentage());
    }

    @Test
    void withdrawCannotExceedAccumulatedAmount() {
        PersonalSavingsGoal goal = goal(7L, 100_000, 1_000_000);
        when(goalRepository.findOwnedForUpdate(10L, 7L)).thenReturn(Optional.of(goal));

        assertThrows(IllegalArgumentException.class, () -> service.contribute(
                7L,
                10L,
                new SavingsContributionRequest(BigDecimal.valueOf(150_000), SavingsOperation.WITHDRAW)
        ));
        verify(goalRepository, never()).save(any());
    }

    @Test
    void updateCannotAccessAnotherUsersGoal() {
        when(goalRepository.findOwnedForUpdate(10L, 7L)).thenReturn(Optional.empty());
        SavingsGoalRequest request = new SavingsGoalRequest(
                "Laptop", "💻", BigDecimal.valueOf(20_000_000), null
        );

        assertThrows(IllegalArgumentException.class, () -> service.updateGoal(7L, 10L, request));
    }

    private PersonalSavingsGoal goal(Long userId, long current, long target) {
        return PersonalSavingsGoal.builder()
                .id(10L)
                .user(User.builder().id(userId).build())
                .title("Laptop")
                .emoji("💻")
                .currentAmount(BigDecimal.valueOf(current))
                .targetAmount(BigDecimal.valueOf(target))
                .status(SavingsGoalStatus.ACTIVE)
                .build();
    }
}
