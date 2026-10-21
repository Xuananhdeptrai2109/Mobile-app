package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.MonthlyBudgetRequest;
import com.dev.expensemanagement.entity.*;
import com.dev.expensemanagement.repository.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonalInsightsServiceTest {
    private PersonalTransactionRepository transactionRepository;
    private PersonalMonthlyBudgetRepository budgetRepository;
    private UserRepository userRepository;
    private PersonalInsightsService service;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(PersonalTransactionRepository.class);
        budgetRepository = mock(PersonalMonthlyBudgetRepository.class);
        userRepository = mock(UserRepository.class);
        service = new PersonalInsightsService(transactionRepository, budgetRepository, userRepository);
    }

    @Test
    void statisticsAggregatesExpenseCategoriesAndDailyValues() {
        User user = User.builder().id(7L).build();
        PersonalCategory food = PersonalCategory.builder()
                .id(1L).name("Ăn uống").iconName("fastfood").colorCode("#F97316").build();
        List<PersonalTransaction> transactions = List.of(
                transaction(user, food, PersonalTransactionType.EXPENSE, 100_000, 2),
                transaction(user, food, PersonalTransactionType.EXPENSE, 50_000, 2),
                transaction(user, food, PersonalTransactionType.INCOME, 500_000, 5)
        );
        LocalDateTime from = LocalDateTime.of(2026, 10, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 11, 1, 0, 0);
        when(transactionRepository
                .findByUser_IdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeAsc(
                        7L, from, to
                )).thenReturn(transactions);
        when(transactionRepository.sumAmount(
                7L, PersonalTransactionType.EXPENSE,
                LocalDateTime.of(2026, 9, 1, 0, 0), from
        )).thenReturn(BigDecimal.valueOf(100_000));
        when(budgetRepository.findByUser_IdAndMonth(7L, LocalDate.of(2026, 10, 1)))
                .thenReturn(Optional.of(PersonalMonthlyBudget.builder()
                        .id(9L).amount(BigDecimal.valueOf(300_000)).build()));

        var result = service.getStatistics(7L, "2026-10");

        assertEquals(BigDecimal.valueOf(500_000), result.getIncome());
        assertEquals(BigDecimal.valueOf(150_000), result.getExpense());
        assertEquals(new BigDecimal("70.00"), result.getSavingsRate());
        assertEquals(new BigDecimal("50.00"), result.getExpenseChangePercentage());
        assertEquals(1, result.getExpenseByCategory().size());
        assertEquals(2, result.getExpenseByCategory().get(0).getTransactionCount());
        assertEquals(BigDecimal.valueOf(150_000), result.getDailyExpenses().get(0).getAmount());
        assertEquals(new BigDecimal("50.00"), result.getBudget().getUsagePercentage());
    }

    @Test
    void upsertBudgetIsScopedToAuthenticatedUserAndMonth() {
        User user = User.builder().id(7L).build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(budgetRepository.findByUser_IdAndMonth(7L, LocalDate.of(2026, 10, 1)))
                .thenReturn(Optional.empty());
        when(transactionRepository.sumAmount(anyLong(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        service.upsertBudget(7L, "2026-10", new MonthlyBudgetRequest(BigDecimal.valueOf(4_000_000)));

        verify(budgetRepository).save(argThat(budget ->
                budget.getUser() == user && budget.getMonth().equals(LocalDate.of(2026, 10, 1))
                        && budget.getAmount().equals(BigDecimal.valueOf(4_000_000))
        ));
    }

    private PersonalTransaction transaction(
            User user,
            PersonalCategory category,
            PersonalTransactionType type,
            long amount,
            int day
    ) {
        return PersonalTransaction.builder()
                .user(user)
                .category(category)
                .type(type)
                .amount(BigDecimal.valueOf(amount))
                .transactionTime(LocalDateTime.of(2026, 10, day, 12, 0))
                .build();
    }
}
