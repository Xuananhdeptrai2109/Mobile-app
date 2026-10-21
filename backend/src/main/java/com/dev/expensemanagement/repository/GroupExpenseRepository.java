package com.dev.expensemanagement.repository;

import com.dev.expensemanagement.entity.GroupExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupExpenseRepository extends JpaRepository<GroupExpense, Long> {
    List<GroupExpense> findByGroup_IdOrderByExpenseDateDescCreatedAtDesc(Long groupId);

    Optional<GroupExpense> findByIdAndGroup_Id(Long expenseId, Long groupId);

    boolean existsByPaidBy_Id(Long memberId);
}
