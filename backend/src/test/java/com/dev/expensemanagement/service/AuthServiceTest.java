package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.RegisterRequest;
import com.dev.expensemanagement.entity.User;
import com.dev.expensemanagement.repository.UserRepository;
import com.dev.expensemanagement.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Test
    void registerCreatesDefaultPersonalWallet() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        PersonalWalletService walletService = mock(PersonalWalletService.class);
        NotificationService notificationService = mock(NotificationService.class);
        AuthService service = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                walletService,
                notificationService
        );
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });
        when(jwtService.issueToken(any(User.class))).thenReturn("token");
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Nguyễn An")
                .email("an@example.com")
                .password("secret1")
                .confirmPassword("secret1")
                .build();

        var response = service.register(request);

        assertEquals(7L, response.getUserId());
        verify(walletService).createDefaultWallet(argThat(user -> user.getId().equals(7L)));
        verify(notificationService).createWelcomeNotification(argThat(user -> user.getId().equals(7L)));
    }
}
