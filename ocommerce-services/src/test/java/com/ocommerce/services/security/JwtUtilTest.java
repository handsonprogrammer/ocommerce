package com.ocommerce.services.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        jwtUtil = org.mockito.Mockito.spy(new JwtUtil());
        java.lang.reflect.Field accessTokenExpField = JwtUtil.class.getDeclaredField("accessTokenExpirationMs");
        accessTokenExpField.setAccessible(true);
        accessTokenExpField.set(jwtUtil, 3600L);

        java.lang.reflect.Field refreshTokenExpField = JwtUtil.class.getDeclaredField("refreshTokenExpirationMs");
        refreshTokenExpField.setAccessible(true);
        refreshTokenExpField.set(jwtUtil, 86400L);

        java.lang.reflect.Field jwtSecretField = JwtUtil.class.getDeclaredField("jwtSecret");
        jwtSecretField.setAccessible(true);
        jwtSecretField.set(jwtUtil, "bXlTdXBlclNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbkFuZFZhbGlkYXRpb25JbkVDb21tZXJjZVNlcnZpY2Vz"); // 32 characters for HMAC-SHA256
    }

    @Test
    @DisplayName("Generate access token - should create valid JWT token")
    void generateAccessToken_WithAuthentication_ShouldReturnValidToken() {
        // Given
        String email = "test@example.com";
        UserDetails userDetails = User.builder()
                .username(email)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // When
        String token = jwtUtil.generateAccessToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        assertThat(jwtUtil.extractUsername(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("Generate access token - should create valid JWT token with username")
    void generateAccessToken_WithUsername_ShouldReturnValidToken() {
        // Given
        String email = "test@example.com";

        // When
        String token = jwtUtil.generateAccessToken(email);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        assertThat(jwtUtil.extractUsername(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("Extract username from token - should return correct username")
    void extractUsername_WithValidToken_ShouldReturnUsername() {
        // Given
        String email = "test@example.com";
        String token = jwtUtil.generateAccessToken(email);

        // When
        String extractedEmail = jwtUtil.extractUsername(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("Validate token - with valid token and matching user details should return true")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String email = "test@example.com";
        String token = jwtUtil.generateAccessToken(email);
        UserDetails userDetails = User.builder()
                .username(email)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Validate token - with different username should return false")
    void validateToken_WithDifferentUsername_ShouldReturnFalse() {
        // Given
        String email = "test@example.com";
        String differentEmail = "different@example.com";
        String token = jwtUtil.generateAccessToken(email);
        UserDetails userDetails = User.builder()
                .username(differentEmail)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Validate token - with malformed token should return false")
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        // Given
        String malformedToken = "not.a.jwt.token";
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        boolean isValid = jwtUtil.validateToken(malformedToken, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Generate refresh token - should create valid refresh token")
    void generateRefreshToken_WithUsername_ShouldReturnValidToken() {
        // Given
        String email = "test@example.com";

        // When
        String refreshToken = jwtUtil.generateRefreshToken(email);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.split("\\.")).hasSize(3); // JWT has 3 parts
        assertThat(jwtUtil.extractUsername(refreshToken)).isEqualTo(email);
        assertThat(jwtUtil.isRefreshToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("Extract expiration - should return future date for valid token")
    void extractExpiration_WithValidToken_ShouldReturnFutureDate() {
        // Given
        String email = "test@example.com";
        String token = jwtUtil.generateAccessToken(email);

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Is token expired - with fresh token should return false")
    void isTokenExpired_WithFreshToken_ShouldReturnFalse() {
        // Given
        String email = "test@example.com";
        String token = jwtUtil.generateAccessToken(email);

        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Is access token - with access token should return true")
    void isAccessToken_WithAccessToken_ShouldReturnTrue() {
        // Given
        String email = "test@example.com";
        String token = jwtUtil.generateAccessToken(email);

        // When
        boolean isAccessToken = jwtUtil.isAccessToken(token);

        // Then
        assertThat(isAccessToken).isTrue();
    }

    @Test
    @DisplayName("Is refresh token - with access token should return false")
    void isRefreshToken_WithAccessToken_ShouldReturnFalse() {
        // Given
        String email = "test@example.com";
        String token = jwtUtil.generateAccessToken(email);

        // When
        boolean isRefreshToken = jwtUtil.isRefreshToken(token);

        // Then
        assertThat(isRefreshToken).isFalse();
    }

    @Test
    @DisplayName("Validate token - with token only should validate structure")
    void validateToken_WithTokenOnly_ShouldValidateStructure() {
        // Given
        String email = "test@example.com";
        String token = jwtUtil.generateAccessToken(email);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Validate token - with invalid token should return false")
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Get token expiration values - should return positive values")
    void getExpirationValues_ShouldReturnPositiveValues() {
        // When/Then
        assertThat(jwtUtil.getAccessTokenExpirationSeconds()).isPositive();
        assertThat(jwtUtil.getRefreshTokenExpirationSeconds()).isPositive();
        assertThat(jwtUtil.getRefreshTokenExpiration()).isAfter(java.time.LocalDateTime.now());
    }

    @Test
    @DisplayName("Extract token type - should return correct type")
    void extractTokenType_ShouldReturnCorrectType() {
        // Given
        String email = "test@example.com";
        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        // When/Then
        assertThat(jwtUtil.extractTokenType(accessToken)).isEqualTo("access");
        assertThat(jwtUtil.extractTokenType(refreshToken)).isEqualTo("refresh");
    }
}
