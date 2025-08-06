package com.ocommerce.services.user.service;

import com.ocommerce.services.user.domain.RefreshToken;
import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.AuthResponse;
import com.ocommerce.services.user.dto.LoginRequest;
import com.ocommerce.services.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private LoginRequest loginRequest;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        loginRequest = createLoginRequest();
        refreshToken = createRefreshToken();
    }

    @Test
    void login_WhenCredentialsValid_ShouldReturnAuthResponse() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateAccessToken(authentication))
                .thenReturn("access-token");
        when(jwtUtil.getAccessTokenExpirationSeconds())
                .thenReturn(3600L);
        when(refreshTokenService.createRefreshToken(testUser))
                .thenReturn(refreshToken);

        // When
        AuthResponse result = authenticationService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken.getToken());
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getExpiresIn()).isEqualTo(3600L);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateAccessToken(authentication);
        verify(jwtUtil).getAccessTokenExpirationSeconds();
        verify(refreshTokenService).createRefreshToken(testUser);
    }

    @Test
    @DisplayName("Login - Invalid credentials should throw BadCredentialsException")
    void login_WhenCredentialsInvalid_ShouldThrowBadCredentialsException() {
        // Given
        String email = "test@example.com";
        String password = "wrongpassword";
        LoginRequest loginRequest = new LoginRequest(email, password);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        
        // When/Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }    @Test
    void login_WhenUserNotFound_ShouldThrowException() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // When/Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(RuntimeException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void refreshToken_WhenTokenValid_ShouldReturnNewAuthResponse() {
        // Given
        String refreshTokenString = "valid-refresh-token";
        when(refreshTokenService.verifyToken(refreshTokenString))
                .thenReturn(refreshToken);
        when(jwtUtil.generateAccessToken(testUser.getEmail()))
                .thenReturn("new-access-token");
        when(jwtUtil.getAccessTokenExpirationSeconds())
                .thenReturn(3600L);
        when(refreshTokenService.createRefreshToken(testUser))
                .thenReturn(refreshToken);

        // When
        AuthResponse result = authenticationService.refreshToken(refreshTokenString);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken.getToken());
        assertThat(result.getExpiresIn()).isEqualTo(3600L);

        verify(refreshTokenService).verifyToken(refreshTokenString);
        verify(refreshTokenService).revokeToken(refreshTokenString);
        verify(jwtUtil).generateAccessToken(testUser.getEmail());
        verify(jwtUtil).getAccessTokenExpirationSeconds();
        verify(refreshTokenService).createRefreshToken(testUser);
    }

    @Test
    void refreshToken_WhenTokenInvalid_ShouldThrowException() {
        // Given
        String refreshTokenString = "invalid-refresh-token";
        when(refreshTokenService.verifyToken(refreshTokenString))
                .thenThrow(new RefreshTokenService.InvalidRefreshTokenException("Invalid refresh token"));

        // When/Then
        assertThatThrownBy(() -> authenticationService.refreshToken(refreshTokenString))
                .isInstanceOf(RefreshTokenService.InvalidRefreshTokenException.class);

        verify(refreshTokenService).verifyToken(refreshTokenString);
    }

    @Test
    void refreshToken_WhenTokenExpired_ShouldThrowException() {
        // Given
        String refreshTokenString = "expired-refresh-token";
        when(refreshTokenService.verifyToken(refreshTokenString))
                .thenThrow(new RefreshTokenService.InvalidRefreshTokenException("Token expired"));

        // When/Then
        assertThatThrownBy(() -> authenticationService.refreshToken(refreshTokenString))
                .isInstanceOf(RefreshTokenService.InvalidRefreshTokenException.class)
                .hasMessageContaining("Token expired");

        verify(refreshTokenService).verifyToken(refreshTokenString);
    }

    @Test
    void logout_WhenValidToken_ShouldRevokeToken() {
        // Given
        String refreshTokenString = "valid-refresh-token";
        when(refreshTokenService.findByToken(refreshTokenString))
                .thenReturn(Optional.of(refreshToken));

        // When
        authenticationService.logout(refreshTokenString);

        // Then
        verify(refreshTokenService).findByToken(refreshTokenString);
        verify(refreshTokenService).revokeToken(refreshTokenString);
    }

    @Test
    void logout_WhenInvalidToken_ShouldNotThrow() {
        // Given
        String refreshTokenString = "invalid-refresh-token";
        when(refreshTokenService.findByToken(refreshTokenString))
                .thenReturn(Optional.empty());

        // When/Then - should not throw exception
        assertThatCode(() -> authenticationService.logout(refreshTokenString))
                .doesNotThrowAnyException();

        verify(refreshTokenService).findByToken(refreshTokenString);
        verify(refreshTokenService, never()).revokeToken(anyString());
    }

    // Helper methods
    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setAccountEnabled(true);
        return user;
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        return request;
    }

    private RefreshToken createRefreshToken() {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID());
        token.setUser(testUser);
        token.setToken("refresh-token-value");
        token.setExpiryDate(LocalDateTime.now().plusDays(1));
        return token;
    }
}
