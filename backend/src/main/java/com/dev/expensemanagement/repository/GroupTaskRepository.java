package com.dev.expensemanagement.repository;

import com.dev.expensemanagement.entity.GroupTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupTaskRepository extends JpaRepository<GroupTask, Long> {
    List<GroupTask> findByGroup_Id(Long groupId);

    Optional<GroupTask> findByIdAndGroup_Id(Long taskId, Long groupId);
}
