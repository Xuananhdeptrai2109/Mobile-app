package com.dev.expensemanagement.repository;

import com.dev.expensemanagement.entity.PersonalSavingsGoal;
import com.dev.expensemanagement.entity.SavingsGoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface PersonalSavingsGoalRepository extends JpaRepository<PersonalSavingsGoal, Long> {
    List<PersonalSavingsGoal> findByUser_IdOrderByStatusAscCreatedAtDesc(Long userId);
    List<PersonalSavingsGoal> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, SavingsGoalStatus status);
    Optional<PersonalSavingsGoal> findByIdAndUser_Id(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from PersonalSavingsGoal g where g.id = :goalId and g.user.id = :userId")
    Optional<PersonalSavingsGoal> findOwnedForUpdate(
            @Param("goalId") Long goalId,
            @Param("userId") Long userId
    );
}
