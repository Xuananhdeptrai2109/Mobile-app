package com.dev.expensemanagement.repository;

import com.dev.expensemanagement.entity.PersonalWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PersonalWalletRepository extends JpaRepository<PersonalWallet, Long> {
    List<PersonalWallet> findByUser_IdOrderByIsDefaultDescCreatedAtAsc(Long userId);
    Optional<PersonalWallet> findByIdAndUser_Id(Long id, Long userId);
    Optional<PersonalWallet> findByUser_IdAndIsDefaultTrue(Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from PersonalWallet w where w.id = :walletId and w.user.id = :userId")
    Optional<PersonalWallet> findOwnedForUpdate(
            @Param("walletId") Long walletId,
            @Param("userId") Long userId
    );

    @Query("select coalesce(sum(w.balance), 0) from PersonalWallet w where w.user.id = :userId")
    BigDecimal sumBalanceByUserId(@Param("userId") Long userId);
}
