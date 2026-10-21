package com.dev.expensemanagement.repository;

import com.dev.expensemanagement.entity.PersonalCategory;
import com.dev.expensemanagement.entity.PersonalTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalCategoryRepository extends JpaRepository<PersonalCategory, Long> {
    List<PersonalCategory> findByIsActiveTrueOrderByTypeAscDisplayOrderAsc();
    List<PersonalCategory> findByTypeAndIsActiveTrueOrderByDisplayOrderAsc(PersonalTransactionType type);
    Optional<PersonalCategory> findByIdAndIsActiveTrue(Long id);
}
