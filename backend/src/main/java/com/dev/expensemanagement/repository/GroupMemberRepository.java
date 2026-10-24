package com.dev.expensemanagement.repository;

import com.dev.expensemanagement.entity.ExpenseGroup;
import com.dev.expensemanagement.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    boolean existsByGroupAndName(ExpenseGroup group, String name);

    List<GroupMember> findByGroup_Id(Long groupId);

    List<GroupMember> findByUser_Id(Long userId);

    Optional<GroupMember> findByGroup_IdAndUser_Id(Long groupId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from GroupMember m where m.group.id = :groupId and m.user.id = :userId")
    Optional<GroupMember> findByGroupIdAndUserIdForUpdate(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId
    );

    Optional<GroupMember> findByIdAndGroup_Id(Long memberId, Long groupId);
}
