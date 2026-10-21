package com.dev.expensemanagement.repository;

import com.dev.expensemanagement.entity.GroupExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupExpenseSplitRepository extends JpaRepository<GroupExpenseSplit, Long> {
    List<GroupExpenseSplit> findByExpense_Id(Long expenseId);

    List<GroupExpenseSplit> findByExpense_Group_Id(Long groupId);

    boolean existsByMember_Id(Long memberId);

    Optional<GroupExpenseSplit> findByIdAndExpense_IdAndExpense_Group_Id(
            Long splitId,
            Long expenseId,
            Long groupId
    );
}
