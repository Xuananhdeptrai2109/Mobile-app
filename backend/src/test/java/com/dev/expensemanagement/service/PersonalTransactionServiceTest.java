package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.PersonalTransactionRequest;
import com.dev.expensemanagement.entity.PersonalCategory;
import com.dev.expensemanagement.entity.PersonalTransaction;
import com.dev.expensemanagement.entity.PersonalTransactionType;
import com.dev.expensemanagement.entity.PersonalWallet;
import com.dev.expensemanagement.entity.User;
import com.dev.expensemanagement.repository.PersonalCategoryRepository;
import com.dev.expensemanagement.repository.PersonalTransactionRepository;
import com.dev.expensemanagement.repository.PersonalWalletRepository;
import com.dev.expensemanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PersonalTransactionServiceTest {

    private PersonalTransactionRepository transactionRepository;
    private PersonalWalletRepository walletRepository;
    private PersonalCategoryRepository categoryRepository;
    private UserRepository userRepository;
    private PersonalTransactionService service;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(PersonalTransactionRepository.class);
        walletRepository = mock(PersonalWalletRepository.class);
        categoryRepository = mock(PersonalCategoryRepository.class);
        userRepository = mock(UserRepository.class);
        service = new PersonalTransactionService(
                transactionRepository,
                walletRepository,
                categoryRepository,
                userRepository
        );
    }

    @Test
    void createExpenseSubtractsOwnedWalletBalance() {
        User user = User.builder().id(7L).build();
        PersonalWallet wallet = wallet(11L, user, 1_000_000);
        PersonalCategory category = category(21L, PersonalTransactionType.EXPENSE);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(walletRepository.findOwnedForUpdate(11L, 7L)).thenReturn(Optional.of(wallet));
        when(categoryRepository.findByIdAndIsActiveTrue(21L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(PersonalTransaction.class))).thenAnswer(invocation -> {
            PersonalTransaction transaction = invocation.getArgument(0);
            transaction.setId(31L);
            return transaction;
        });

        var response = service.createTransaction(
                7L,
                request(11L, 21L, PersonalTransactionType.EXPENSE, 150_000)
        );

        assertEquals(new BigDecimal("850000"), wallet.getBalance());
        assertEquals(31L, response.getTransactionId());
        verify(walletRepository).findOwnedForUpdate(11L, 7L);
        verify(walletRepository).save(wallet);
    }

    @Test
    void updateReversesOldEffectAndAppliesNewEffectAcrossWallets() {
        User user = User.builder().id(7L).build();
        PersonalWallet oldWallet = wallet(11L, user, 900_000);
        PersonalWallet newWallet = wallet(12L, user, 500_000);
        PersonalCategory expenseCategory = category(21L, PersonalTransactionType.EXPENSE);
        PersonalTransaction transaction = transaction(
                31L,
                user,
                oldWallet,
                expenseCategory,
                PersonalTransactionType.EXPENSE,
                100_000
        );
        when(transactionRepository.findOwnedForUpdate(31L, 7L)).thenReturn(Optional.of(transaction));
        when(walletRepository.findOwnedForUpdate(11L, 7L)).thenReturn(Optional.of(oldWallet));
        when(walletRepository.findOwnedForUpdate(12L, 7L)).thenReturn(Optional.of(newWallet));
        when(categoryRepository.findByIdAndIsActiveTrue(21L)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        service.updateTransaction(
                7L,
                31L,
                request(12L, 21L, PersonalTransactionType.EXPENSE, 50_000)
        );

        assertEquals(new BigDecimal("1000000"), oldWallet.getBalance());
        assertEquals(new BigDecimal("450000"), newWallet.getBalance());
        assertEquals(12L, transaction.getWallet().getId());
        assertEquals(new BigDecimal("50000"), transaction.getAmount());
        verify(walletRepository).saveAll(anyCollection());
    }

    @Test
    void deleteExpenseRestoresWalletBalance() {
        User user = User.builder().id(7L).build();
        PersonalWallet wallet = wallet(11L, user, 900_000);
        PersonalCategory category = category(21L, PersonalTransactionType.EXPENSE);
        PersonalTransaction transaction = transaction(
                31L,
                user,
                wallet,
                category,
                PersonalTransactionType.EXPENSE,
                100_000
        );
        when(transactionRepository.findOwnedForUpdate(31L, 7L)).thenReturn(Optional.of(transaction));
        when(walletRepository.findOwnedForUpdate(11L, 7L)).thenReturn(Optional.of(wallet));

        service.deleteTransaction(7L, 31L);

        assertEquals(new BigDecimal("1000000"), wallet.getBalance());
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void createRejectsWalletOwnedByAnotherUser() {
        User user = User.builder().id(7L).build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(walletRepository.findOwnedForUpdate(99L, 7L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(
                7L,
                request(99L, 21L, PersonalTransactionType.EXPENSE, 100_000)
        ));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createRejectsCategoryWithDifferentTypeBeforeChangingBalance() {
        User user = User.builder().id(7L).build();
        PersonalWallet wallet = wallet(11L, user, 1_000_000);
        PersonalCategory incomeCategory = category(21L, PersonalTransactionType.INCOME);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(walletRepository.findOwnedForUpdate(11L, 7L)).thenReturn(Optional.of(wallet));
        when(categoryRepository.findByIdAndIsActiveTrue(21L)).thenReturn(Optional.of(incomeCategory));

        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(
                7L,
                request(11L, 21L, PersonalTransactionType.EXPENSE, 100_000)
        ));

        assertEquals(new BigDecimal("1000000"), wallet.getBalance());
        verify(walletRepository, never()).save(wallet);
    }

    @Test
    void invalidMonthIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.getDashboard(7L, "10/2026"));
        verifyNoInteractions(transactionRepository, walletRepository);
    }

    @Test
    void dashboardUsesAuthenticatedUserAndRequestedMonth() {
        User user = User.builder().id(7L).build();
        PersonalWallet wallet = wallet(11L, user, 1_700_000);
        PersonalCategory category = category(21L, PersonalTransactionType.EXPENSE);
        PersonalTransaction recent = transaction(
                31L,
                user,
                wallet,
                category,
                PersonalTransactionType.EXPENSE,
                300_000
        );
        LocalDateTime from = LocalDateTime.of(2026, 10, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 11, 1, 0, 0);
        when(walletRepository.sumBalanceByUserId(7L)).thenReturn(BigDecimal.valueOf(1_700_000));
        when(transactionRepository.sumAmount(7L, PersonalTransactionType.INCOME, from, to))
                .thenReturn(BigDecimal.valueOf(2_000_000));
        when(transactionRepository.sumAmount(7L, PersonalTransactionType.EXPENSE, from, to))
                .thenReturn(BigDecimal.valueOf(300_000));
        when(transactionRepository
                .findTop5ByUser_IdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeDescCreatedAtDesc(
                        7L,
                        from,
                        to
                )).thenReturn(List.of(recent));

        var dashboard = service.getDashboard(7L, "2026-10");

        assertEquals("2026-10", dashboard.getMonth());
        assertEquals(BigDecimal.valueOf(1_700_000), dashboard.getBalance());
        assertEquals(BigDecimal.valueOf(2_000_000), dashboard.getIncome());
        assertEquals(BigDecimal.valueOf(300_000), dashboard.getExpense());
        assertEquals(31L, dashboard.getRecentTransactions().get(0).getTransactionId());
    }

    @Test
    void transactionSearchIsScopedToAuthenticatedUserAndMonth() {
        LocalDateTime from = LocalDateTime.of(2026, 10, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 11, 1, 0, 0);
        when(transactionRepository.search(
                eq(7L),
                eq(from),
                eq(to),
                eq(PersonalTransactionType.EXPENSE),
                eq(21L),
                eq("cơm"),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        var result = service.getTransactions(
                7L,
                "2026-10",
                PersonalTransactionType.EXPENSE,
                21L,
                "  cơm  ",
                0,
                20
        );

        assertEquals(0, result.getTotalElements());
        verify(transactionRepository).search(
                eq(7L),
                eq(from),
                eq(to),
                eq(PersonalTransactionType.EXPENSE),
                eq(21L),
                eq("cơm"),
                any(Pageable.class)
        );
    }

    private PersonalTransactionRequest request(
            Long walletId,
            Long categoryId,
            PersonalTransactionType type,
            long amount
    ) {
        return PersonalTransactionRequest.builder()
                .title("Giao dịch")
                .amount(BigDecimal.valueOf(amount))
                .type(type)
                .categoryId(categoryId)
                .walletId(walletId)
                .transactionTime(LocalDateTime.of(2026, 10, 14, 12, 0))
                .build();
    }

    private PersonalWallet wallet(Long id, User user, long balance) {
        return PersonalWallet.builder()
                .id(id)
                .user(user)
                .name("Ví " + id)
                .balance(BigDecimal.valueOf(balance))
                .currency("VND")
                .isDefault(id == 11L)
                .build();
    }

    private PersonalCategory category(Long id, PersonalTransactionType type) {
        return PersonalCategory.builder()
                .id(id)
                .name("Danh mục")
                .type(type)
                .iconName("receipt")
                .colorCode("#000000")
                .displayOrder(1)
                .isActive(true)
                .build();
    }

    private PersonalTransaction transaction(
            Long id,
            User user,
            PersonalWallet wallet,
            PersonalCategory category,
            PersonalTransactionType type,
            long amount
    ) {
        return PersonalTransaction.builder()
                .id(id)
                .user(user)
                .wallet(wallet)
                .category(category)
                .title("Cũ")
                .amount(BigDecimal.valueOf(amount))
                .type(type)
                .transactionTime(LocalDateTime.of(2026, 10, 13, 12, 0))
                .build();
    }
}
