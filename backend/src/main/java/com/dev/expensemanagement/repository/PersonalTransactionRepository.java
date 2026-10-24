package com.dev.expensemanagement.repository;

import com.dev.expensemanagement.entity.PersonalTransaction;
import com.dev.expensemanagement.entity.PersonalTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

public interface PersonalTransactionRepository extends JpaRepository<PersonalTransaction, Long> {
    Optional<PersonalTransaction> findByUser_IdAndSourceGroupId(Long userId, Long sourceGroupId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PersonalTransaction t where t.user.id = :userId and t.sourceGroupId = :sourceGroupId")
    Optional<PersonalTransaction> findByUserIdAndSourceGroupIdForUpdate(
            @Param("userId") Long userId,
            @Param("sourceGroupId") Long sourceGroupId
    );
    Optional<PersonalTransaction> findByIdAndUser_Id(Long id, Long userId);
    boolean existsByWallet_Id(Long walletId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PersonalTransaction t where t.id = :transactionId and t.user.id = :userId")
    Optional<PersonalTransaction> findOwnedForUpdate(
            @Param("transactionId") Long transactionId,
            @Param("userId") Long userId
    );

    @Query("""
            select t from PersonalTransaction t
            where t.user.id = :userId
              and t.transactionTime >= :fromTime
              and t.transactionTime < :toTime
              and (:type is null or t.type = :type)
              and (:categoryId is null or t.category.id = :categoryId)
              and (:queryText = ''
                   or lower(t.title) like lower(concat('%', :queryText, '%'))
                   or lower(coalesce(t.note, '')) like lower(concat('%', :queryText, '%')))
            """)
    Page<PersonalTransaction> search(
            @Param("userId") Long userId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("type") PersonalTransactionType type,
            @Param("categoryId") Long categoryId,
            @Param("queryText") String queryText,
            Pageable pageable
    );

    @Query("""
            select coalesce(sum(t.amount), 0) from PersonalTransaction t
            where t.user.id = :userId
              and t.type = :type
              and t.transactionTime >= :fromTime
              and t.transactionTime < :toTime
            """)
    BigDecimal sumAmount(
            @Param("userId") Long userId,
            @Param("type") PersonalTransactionType type,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    List<PersonalTransaction> findTop5ByUser_IdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeDescCreatedAtDesc(
            Long userId,
            LocalDateTime fromTime,
            LocalDateTime toTime
    );

    List<PersonalTransaction> findByUser_IdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeAsc(
            Long userId,
            LocalDateTime fromTime,
            LocalDateTime toTime
    );
}
