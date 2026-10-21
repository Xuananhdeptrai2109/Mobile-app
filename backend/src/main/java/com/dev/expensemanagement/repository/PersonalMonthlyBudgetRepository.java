package com.dev.expensemanagement.repository;

import com.dev.expensemanagement.entity.PersonalMonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PersonalMonthlyBudgetRepository extends JpaRepository<PersonalMonthlyBudget, Long> {
    Optional<PersonalMonthlyBudget> findByUser_IdAndMonth(Long userId, LocalDate month);
}
