package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.LoginRequest;
import com.dev.expensemanagement.dto.request.RegisterRequest;
import com.dev.expensemanagement.dto.response.AuthResponse;
import com.dev.expensemanagement.entity.User;
import com.dev.expensemanagement.exception.AuthException;
import com.dev.expensemanagement.repository.UserRepository;
import com.dev.expensemanagement.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PersonalWalletService personalWalletService;
    private final NotificationService notificationService;
    
    /**
     * Đăng ký user mới
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email đã được đăng ký");
        }
        
        // Kiểm tra phone đã tồn tại
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new AuthException("Số điện thoại đã được đăng ký");
        }
        
        // Tạo user mới
        User user = User.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .password(passwordEncoder.encode(request.getPassword()))
            .isActive(true)
            .build();
        
        User savedUser = userRepository.save(user);
        personalWalletService.createDefaultWallet(savedUser);
        notificationService.createWelcomeNotification(savedUser);
        
        return AuthResponse.builder()
            .userId(savedUser.getId())
            .fullName(savedUser.getFullName())
            .email(savedUser.getEmail())
            .phoneNumber(savedUser.getPhoneNumber())
            .token(jwtService.issueToken(savedUser))
            .message("Đăng ký thành công")
            .success(true)
            .build();
    }
    
    /**
     * Đăng nhập
     */
    public AuthResponse login(LoginRequest request) {
        // Tìm user bằng email hoặc phone
        Optional<User> userOptional = userRepository
            .findByEmailOrPhoneNumber(request.getEmailOrPhoneNumber(), request.getEmailOrPhoneNumber());
        
        if (userOptional.isEmpty()) {
            throw new AuthException("Thông tin đăng nhập không chính xác");
        }
        
        User user = userOptional.get();
        
        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Thông tin đăng nhập không chính xác");
        }
        
        // Kiểm tra user có bị disable
        if (!user.getIsActive()) {
            throw new AuthException("Tài khoản của bạn đã bị khóa");
        }
        
        return AuthResponse.builder()
            .userId(user.getId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .token(jwtService.issueToken(user))
            .message("Đăng nhập thành công")
            .success(true)
            .build();
    }
}
