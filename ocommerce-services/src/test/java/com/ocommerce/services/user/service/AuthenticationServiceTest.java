package com.ocommerce.services.user.service;

import com.ocommerce.services.user.domain.RefreshToken;
import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.AuthResponse;
import com.ocommerce.services.user.dto.LoginRequest;
import com.ocommerce.services.user.dto.SignupRequest;
import com.ocommerce.services.user.dto.UserResponse;
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
        void signup_WhenValidRequest_ShouldReturnAuthResponse() {
                // Given
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setEmail("newuser@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setFirstName("John");
                signupRequest.setLastName("Doe");

                UserResponse userResponse = new UserResponse();
                userResponse.setEmail("newuser@example.com");

                when(userService.registerUser(signupRequest))
                                .thenReturn(userResponse);
                when(userService.findByEmail(signupRequest.getEmail()))
                                .thenReturn(Optional.of(testUser));
                when(jwtUtil.generateAccessToken(testUser.getEmail()))
                                .thenReturn("access-token");
                when(jwtUtil.getAccessTokenExpirationSeconds())
                                .thenReturn(3600L);
                when(refreshTokenService.createRefreshToken(testUser))
                                .thenReturn(refreshToken);

                // When
                AuthResponse result = authenticationService.signup(signupRequest);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getAccessToken()).isEqualTo("access-token");
                assertThat(result.getRefreshToken()).isEqualTo(refreshToken.getToken());
                assertThat(result.getExpiresIn()).isEqualTo(3600L);

                verify(userService).registerUser(signupRequest);
                verify(userService).findByEmail(signupRequest.getEmail());
                verify(jwtUtil).generateAccessToken(testUser.getEmail());
                verify(jwtUtil).getAccessTokenExpirationSeconds();
                verify(refreshTokenService).createRefreshToken(testUser);
        }

        @Test
        void signup_WhenUserRegistrationFails_ShouldThrowException() {
                // Given
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setEmail("newuser@example.com");
                signupRequest.setPassword("password123");

                UserResponse userResponse = new UserResponse();
                userResponse.setEmail("newuser@example.com");

                when(userService.registerUser(signupRequest))
                                .thenReturn(userResponse);
                when(userService.findByEmail(signupRequest.getEmail()))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> authenticationService.signup(signupRequest))
                                .isInstanceOf(RuntimeException.class)
                                .hasMessage("User registration failed");

                verify(userService).registerUser(signupRequest);
                verify(userService).findByEmail(signupRequest.getEmail());
                verify(jwtUtil, never()).generateAccessToken(anyString());
                verify(refreshTokenService, never()).createRefreshToken(any(User.class));
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
        }

        @Test
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
        void refreshTokenForUser_WhenTokenValid_ShouldReturnNewAuthResponse() {
                // Given
                String email = "test@example.com";
                String refreshTokenString = "valid-refresh-token";
                when(userService.findByEmail(email))
                                .thenReturn(Optional.of(testUser));
                when(refreshTokenService.findByUserAndToken(testUser, refreshTokenString))
                                .thenReturn(Optional.of(refreshToken));
                when(jwtUtil.generateAccessToken(testUser.getEmail()))
                                .thenReturn("new-access-token");
                when(jwtUtil.getAccessTokenExpirationSeconds())
                                .thenReturn(3600L);
                when(refreshTokenService.createRefreshToken(testUser))
                                .thenReturn(refreshToken);

                // When
                AuthResponse result = authenticationService.refreshTokenForUser(email, refreshTokenString);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getAccessToken()).isEqualTo("new-access-token");
                assertThat(result.getRefreshToken()).isEqualTo(refreshToken.getToken());
                assertThat(result.getExpiresIn()).isEqualTo(3600L);

                verify(userService).findByEmail(email);
                verify(refreshTokenService).findByUserAndToken(testUser, refreshTokenString);
                verify(refreshTokenService).revokeTokenForUser(refreshToken.getToken(), testUser);
                verify(jwtUtil).generateAccessToken(testUser.getEmail());
                verify(jwtUtil).getAccessTokenExpirationSeconds();
                verify(refreshTokenService).createRefreshToken(testUser);
        }

        @Test
        void refreshTokenForUser_WhenTokenInvalid_ShouldThrowException() {
                // Given
                String email = "test@example.com";
                String refreshTokenString = "invalid-refresh-token";
                when(userService.findByEmail(email))
                                .thenReturn(Optional.of(testUser));
                when(refreshTokenService.findByUserAndToken(testUser, refreshTokenString))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> authenticationService.refreshTokenForUser(email, refreshTokenString))
                                .isInstanceOf(RefreshTokenService.InvalidRefreshTokenException.class);

                verify(userService).findByEmail(email);
                verify(refreshTokenService).findByUserAndToken(testUser, refreshTokenString);
        }

        @Test
        void refreshTokenForUser_WhenUserNotFound_ShouldThrowException() {
                // Given
                String email = "notfound@example.com";
                String refreshTokenString = "valid-refresh-token";
                when(userService.findByEmail(email))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> authenticationService.refreshTokenForUser(email, refreshTokenString))
                                .isInstanceOf(UserService.UserNotFoundException.class)
                                .hasMessage("User not found");

                verify(userService).findByEmail(email);
                verify(refreshTokenService, never()).findByUserAndToken(any(), anyString());
        }

        @Test
        void logout_WhenValidToken_ShouldRevokeToken() {
                // Given
                String email = "test@example.com";
                String refreshTokenString = "valid-refresh-token";
                when(userService.findByEmail(email))
                                .thenReturn(Optional.of(testUser));
                when(refreshTokenService.findByUserAndToken(testUser, refreshTokenString))
                                .thenReturn(Optional.of(refreshToken));

                // When
                authenticationService.logout(email, refreshTokenString);

                // Then
                verify(userService).findByEmail(email);
                verify(refreshTokenService).findByUserAndToken(testUser, refreshTokenString);
                verify(refreshTokenService).revokeTokenForUser(refreshToken.getToken(), testUser);
        }

        @Test
        void logout_WhenInvalidToken_ShouldNotThrow() {
                // Given
                String email = "test@example.com";
                String refreshTokenString = "invalid-refresh-token";
                when(userService.findByEmail(email))
                                .thenReturn(Optional.of(testUser));
                when(refreshTokenService.findByUserAndToken(testUser, refreshTokenString))
                                .thenReturn(Optional.empty());

                // When/Then - should not throw exception
                assertThatCode(() -> authenticationService.logout(email, refreshTokenString))
                                .doesNotThrowAnyException();

                verify(userService).findByEmail(email);
                verify(refreshTokenService).findByUserAndToken(testUser, refreshTokenString);
                verify(refreshTokenService, never()).revokeTokenForUser(anyString(), any(User.class));
        }

        @Test
        void logoutFromAllDevices_WhenUserExists_ShouldRevokeAllTokens() {
                // Given
                String userEmail = "test@example.com";
                when(userService.findByEmail(userEmail))
                                .thenReturn(Optional.of(testUser));

                // When
                authenticationService.logoutFromAllDevices(userEmail);

                // Then
                verify(userService).findByEmail(userEmail);
                verify(refreshTokenService).revokeAllTokensForUser(testUser);
        }

        @Test
        void logoutFromAllDevices_WhenUserNotFound_ShouldThrowException() {
                // Given
                String userEmail = "notfound@example.com";
                when(userService.findByEmail(userEmail))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> authenticationService.logoutFromAllDevices(userEmail))
                                .isInstanceOf(RuntimeException.class)
                                .hasMessage("User not found");

                verify(userService).findByEmail(userEmail);
                verify(refreshTokenService, never()).revokeAllTokensForUser(any(User.class));
        }

        @Test
        void validateAccessToken_WhenTokenValid_ShouldReturnTrue() {
                // Given
                String validToken = "valid-access-token";
                when(jwtUtil.validateToken(validToken)).thenReturn(true);
                when(jwtUtil.isAccessToken(validToken)).thenReturn(true);

                // When
                boolean result = authenticationService.validateAccessToken(validToken);

                // Then
                assertThat(result).isTrue();
                verify(jwtUtil).validateToken(validToken);
                verify(jwtUtil).isAccessToken(validToken);
        }

        @Test
        void validateAccessToken_WhenTokenInvalid_ShouldReturnFalse() {
                // Given
                String invalidToken = "invalid-token";
                when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

                // When
                boolean result = authenticationService.validateAccessToken(invalidToken);

                // Then
                assertThat(result).isFalse();
                verify(jwtUtil).validateToken(invalidToken);
                verify(jwtUtil, never()).isAccessToken(anyString());
        }

        @Test
        void extractUserEmail_WhenTokenValid_ShouldReturnEmail() {
                // Given
                String validToken = "valid-access-token";
                String expectedEmail = "test@example.com";
                when(jwtUtil.validateToken(validToken)).thenReturn(true);
                when(jwtUtil.isAccessToken(validToken)).thenReturn(true);
                when(jwtUtil.extractUsername(validToken)).thenReturn(expectedEmail);

                // When
                String result = authenticationService.extractUserEmail(validToken);

                // Then
                assertThat(result).isEqualTo(expectedEmail);
                verify(jwtUtil).validateToken(validToken);
                verify(jwtUtil).isAccessToken(validToken);
                verify(jwtUtil).extractUsername(validToken);
        }

        @Test
        void extractUserEmail_WhenTokenInvalid_ShouldThrowException() {
                // Given
                String invalidToken = "invalid-token";
                when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

                // When/Then
                assertThatThrownBy(() -> authenticationService.extractUserEmail(invalidToken))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Invalid access token");

                verify(jwtUtil).validateToken(invalidToken);
                verify(jwtUtil, never()).extractUsername(anyString());
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
