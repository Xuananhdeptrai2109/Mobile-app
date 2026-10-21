package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.MonthlyBudgetRequest;
import com.dev.expensemanagement.dto.response.*;
import com.dev.expensemanagement.entity.*;
import com.dev.expensemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PersonalInsightsService {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final PersonalTransactionRepository transactionRepository;
    private final PersonalMonthlyBudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PersonalStatisticsResponse getStatistics(Long userId, String month) {
        YearMonth selectedMonth = parseMonth(month);
        LocalDateTime from = selectedMonth.atDay(1).atStartOfDay();
        LocalDateTime to = selectedMonth.plusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime previousFrom = selectedMonth.minusMonths(1).atDay(1).atStartOfDay();

        List<PersonalTransaction> transactions = transactionRepository
                .findByUser_IdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeAsc(
                        userId, from, to
                );
        BigDecimal income = sum(transactions, PersonalTransactionType.INCOME);
        BigDecimal expense = sum(transactions, PersonalTransactionType.EXPENSE);
        BigDecimal previousExpense = transactionRepository.sumAmount(
                userId,
                PersonalTransactionType.EXPENSE,
                previousFrom,
                from
        );

        Map<Long, CategoryAccumulator> categories = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> daily = new TreeMap<>();
        transactions.stream()
                .filter(transaction -> transaction.getType() == PersonalTransactionType.EXPENSE)
                .forEach(transaction -> {
                    PersonalCategory category = transaction.getCategory();
                    categories.computeIfAbsent(category.getId(), ignored -> new CategoryAccumulator(category))
                            .add(transaction.getAmount());
                    daily.merge(transaction.getTransactionTime().toLocalDate(), transaction.getAmount(), BigDecimal::add);
                });

        BigDecimal finalExpense = expense;
        List<CategoryStatisticResponse> categoryResponses = categories.values().stream()
                .sorted(Comparator.comparing(CategoryAccumulator::amount).reversed())
                .map(value -> value.toResponse(finalExpense))
                .toList();
        List<DailyExpenseResponse> dailyResponses = daily.entrySet().stream()
                .map(entry -> DailyExpenseResponse.builder().date(entry.getKey()).amount(entry.getValue()).build())
                .toList();

        return PersonalStatisticsResponse.builder()
                .month(selectedMonth.toString())
                .income(income)
                .expense(expense)
                .net(income.subtract(expense))
                .savingsRate(percentage(income.subtract(expense), income))
                .previousMonthExpense(previousExpense)
                .expenseChangePercentage(changePercentage(expense, previousExpense))
                .budget(toBudgetResponse(userId, selectedMonth, expense))
                .expenseByCategory(categoryResponses)
                .dailyExpenses(dailyResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public MonthlyBudgetResponse getBudget(Long userId, String month) {
        YearMonth selectedMonth = parseMonth(month);
        BigDecimal spent = transactionRepository.sumAmount(
                userId,
                PersonalTransactionType.EXPENSE,
                selectedMonth.atDay(1).atStartOfDay(),
                selectedMonth.plusMonths(1).atDay(1).atStartOfDay()
        );
        return toBudgetResponse(userId, selectedMonth, spent);
    }

    @Transactional
    public MonthlyBudgetResponse upsertBudget(Long userId, String month, MonthlyBudgetRequest request) {
        YearMonth selectedMonth = parseMonth(month);
        LocalDate monthDate = selectedMonth.atDay(1);
        PersonalMonthlyBudget budget = budgetRepository.findByUser_IdAndMonth(userId, monthDate)
                .orElseGet(() -> PersonalMonthlyBudget.builder()
                        .user(userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại")))
                        .month(monthDate)
                        .build());
        budget.setAmount(request.getAmount());
        budgetRepository.save(budget);
        return getBudget(userId, selectedMonth.toString());
    }

    @Transactional
    public void deleteBudget(Long userId, String month) {
        YearMonth selectedMonth = parseMonth(month);
        budgetRepository.findByUser_IdAndMonth(userId, selectedMonth.atDay(1))
                .ifPresent(budgetRepository::delete);
    }

    private MonthlyBudgetResponse toBudgetResponse(Long userId, YearMonth month, BigDecimal spent) {
        Optional<PersonalMonthlyBudget> budget = budgetRepository.findByUser_IdAndMonth(userId, month.atDay(1));
        BigDecimal amount = budget.map(PersonalMonthlyBudget::getAmount).orElse(BigDecimal.ZERO);
        return MonthlyBudgetResponse.builder()
                .budgetId(budget.map(PersonalMonthlyBudget::getId).orElse(null))
                .month(month.toString())
                .amount(amount)
                .spent(spent)
                .remaining(amount.signum() == 0 ? BigDecimal.ZERO : amount.subtract(spent))
                .usagePercentage(percentage(spent, amount))
                .exceeded(amount.signum() > 0 && spent.compareTo(amount) > 0)
                .build();
    }

    private BigDecimal sum(List<PersonalTransaction> transactions, PersonalTransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .map(PersonalTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal percentage(BigDecimal value, BigDecimal total) {
        if (total.signum() == 0) return BigDecimal.ZERO;
        return value.multiply(ONE_HUNDRED).divide(total, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal changePercentage(BigDecimal current, BigDecimal previous) {
        if (previous.signum() == 0) return current.signum() == 0 ? BigDecimal.ZERO : ONE_HUNDRED;
        return current.subtract(previous).multiply(ONE_HUNDRED).divide(previous, 2, RoundingMode.HALF_UP);
    }

    private YearMonth parseMonth(String value) {
        if (value == null || value.isBlank()) return YearMonth.now();
        try {
            return YearMonth.parse(value.trim());
        } catch (DateTimeParseException error) {
            throw new IllegalArgumentException("Tháng phải có định dạng yyyy-MM");
        }
    }

    private static final class CategoryAccumulator {
        private final PersonalCategory category;
        private BigDecimal amount = BigDecimal.ZERO;
        private long count;

        private CategoryAccumulator(PersonalCategory category) {
            this.category = category;
        }

        private void add(BigDecimal value) {
            amount = amount.add(value);
            count++;
        }

        private BigDecimal amount() {
            return amount;
        }

        private CategoryStatisticResponse toResponse(BigDecimal total) {
            BigDecimal percentage = total.signum() == 0
                    ? BigDecimal.ZERO
                    : amount.multiply(ONE_HUNDRED).divide(total, 2, RoundingMode.HALF_UP);
            return CategoryStatisticResponse.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .iconName(category.getIconName())
                    .colorCode(category.getColorCode())
                    .amount(amount)
                    .percentage(percentage)
                    .transactionCount(count)
                    .build();
        }
    }
}
