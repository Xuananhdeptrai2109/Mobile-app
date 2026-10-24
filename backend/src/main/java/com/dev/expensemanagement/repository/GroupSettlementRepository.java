package com.dev.expensemanagement.repository;

import com.dev.expensemanagement.entity.GroupSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupSettlementRepository extends JpaRepository<GroupSettlement, Long> {
    List<GroupSettlement> findByGroup_IdOrderBySettledAtDesc(Long groupId);
}
