package com.dev.expensemanagement.security;

import com.dev.expensemanagement.config.JwtConfig;
import com.dev.expensemanagement.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    @Test
    void issuedTokenCanBeVerifiedAndContainsUserIdAsSubject() {
        JwtConfig config = new JwtConfig();
        SecretKey key = config.jwtSecretKey("test-secret-with-at-least-32-bytes-long");
        JwtEncoder encoder = config.jwtEncoder(key);
        JwtDecoder decoder = config.jwtDecoder(key, "moneycare-test");

        JwtService jwtService = new JwtService(encoder);
        ReflectionTestUtils.setField(jwtService, "issuer", "moneycare-test");
        ReflectionTestUtils.setField(jwtService, "ttlSeconds", 3600L);

        User user = User.builder()
                .id(42L)
                .fullName("Test User")
                .email("test@example.com")
                .build();

        Jwt jwt = decoder.decode(jwtService.issueToken(user));

        assertEquals("42", jwt.getSubject());
        assertEquals("Test User", jwt.getClaimAsString("name"));
        assertEquals("test@example.com", jwt.getClaimAsString("email"));
    }
}
