package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.CreateWalletRequest;
import com.dev.expensemanagement.entity.PersonalWallet;
import com.dev.expensemanagement.entity.User;
import com.dev.expensemanagement.repository.PersonalTransactionRepository;
import com.dev.expensemanagement.repository.PersonalWalletRepository;
import com.dev.expensemanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PersonalWalletServiceTest {

    private PersonalWalletRepository walletRepository;
    private PersonalTransactionRepository transactionRepository;
    private UserRepository userRepository;
    private PersonalWalletService service;

    @BeforeEach
    void setUp() {
        walletRepository = mock(PersonalWalletRepository.class);
        transactionRepository = mock(PersonalTransactionRepository.class);
        userRepository = mock(UserRepository.class);
        service = new PersonalWalletService(walletRepository, transactionRepository, userRepository);
    }

    @Test
    void firstWalletBecomesDefault() {
        User user = User.builder().id(7L).build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser_IdOrderByIsDefaultDescCreatedAtAsc(7L)).thenReturn(List.of());
        when(walletRepository.findByUser_IdAndIsDefaultTrue(7L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(PersonalWallet.class))).thenAnswer(invocation -> {
            PersonalWallet wallet = invocation.getArgument(0);
            wallet.setId(11L);
            return wallet;
        });

        var response = service.createWallet(
                7L,
                CreateWalletRequest.builder().name("Tiền mặt").build()
        );

        assertTrue(response.isDefault());
    }

    @Test
    void defaultWalletCannotBeDeleted() {
        PersonalWallet wallet = PersonalWallet.builder().id(11L).isDefault(true).build();
        when(walletRepository.findOwnedForUpdate(11L, 7L)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class, () -> service.deleteWallet(7L, 11L));

        verify(walletRepository, never()).delete(any());
    }

    @Test
    void walletWithTransactionsCannotBeDeleted() {
        PersonalWallet wallet = PersonalWallet.builder().id(12L).isDefault(false).build();
        when(walletRepository.findOwnedForUpdate(12L, 7L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.existsByWallet_Id(12L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.deleteWallet(7L, 12L));

        verify(walletRepository, never()).delete(any());
    }
}
