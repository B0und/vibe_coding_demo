package com.vibecodingdemo.backend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set test values using reflection since @Value annotations won't work in unit tests
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "myTestSecretKey12345678901234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 86400000L); // 24 hours

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_WithUserDetails_Success() {
        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void generateToken_WithUsername_Success() {
        // Given
        String username = "testuser";

        // When
        String token = jwtUtil.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void extractUsername_Success() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    void extractUsername_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    void extractExpiration_Success() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Should be in the future
    }

    @Test
    void isTokenExpired_ValidToken_ReturnsFalse() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isTokenExpired_InvalidToken_ReturnsTrue() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Boolean isExpired = jwtUtil.isTokenExpired(invalidToken);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isTokenExpired_ExpiredToken_ReturnsTrue() {
        // Given - Create a token with very short expiration
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secretKey", "myTestSecretKey12345678901234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "jwtExpiration", -1000L); // Already expired
        
        String expiredToken = shortExpirationJwtUtil.generateToken(userDetails);

        // When
        Boolean isExpired = jwtUtil.isTokenExpired(expiredToken);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void validateToken_WithUserDetails_ValidToken_ReturnsTrue() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithUserDetails_InvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken, userDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithUserDetails_WrongUser_ReturnsFalse() {
        // Given
        String token = jwtUtil.generateToken(userDetails);
        UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        Boolean isValid = jwtUtil.validateToken(token, differentUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithoutUserDetails_ValidToken_ReturnsTrue() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithoutUserDetails_InvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithoutUserDetails_ExpiredToken_ReturnsFalse() {
        // Given - Create a token with very short expiration
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secretKey", "myTestSecretKey12345678901234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "jwtExpiration", -1000L); // Already expired
        
        String expiredToken = shortExpirationJwtUtil.generateToken(userDetails);

        // When
        Boolean isValid = jwtUtil.validateToken(expiredToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void generateToken_ConsistentResults() {
        // Given
        String username = "testuser";

        // When
        String token1 = jwtUtil.generateToken(username);
        String token2 = jwtUtil.generateToken(username);

        // Then
        // Both tokens should be valid and contain the same username
        assertEquals(jwtUtil.extractUsername(token1), jwtUtil.extractUsername(token2));
        assertTrue(jwtUtil.validateToken(token1));
        assertTrue(jwtUtil.validateToken(token2));
    }
} 