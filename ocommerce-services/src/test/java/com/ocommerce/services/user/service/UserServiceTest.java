package com.ocommerce.services.user.service;

import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.SignupRequest;
import com.ocommerce.services.user.dto.UserResponse;
import com.ocommerce.services.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private SignupRequest signupRequest;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        signupRequest = createSignupRequest();
    }

    @Test
    void registerUser_WhenEmailNotExists_ShouldCreateUser() {
        // Given
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.registerUser(signupRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(signupRequest.getEmail().toLowerCase());
        assertThat(result.getFirstName()).isEqualTo(signupRequest.getFirstName());
        assertThat(result.getLastName()).isEqualTo(signupRequest.getLastName());
        assertThat(result.isAccountEnabled()).isTrue();
        assertThat(result.isEmailVerified()).isFalse();

        verify(userRepository).existsByEmailIgnoreCase(signupRequest.getEmail());
        verify(passwordEncoder).encode(signupRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_WhenEmailExists_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.registerUser(signupRequest))
                .isInstanceOf(UserService.UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository).existsByEmailIgnoreCase(signupRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        verify(userRepository).findByEmailIgnoreCase(email);
    }

    @Test
    void findByEmail_WhenUserNotExists_ShouldReturnEmpty() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmailIgnoreCase(email);
    }

    @Test
    void findEnabledUserByEmail_WhenUserEnabledExists_ShouldReturnUser() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmailIgnoreCaseAndAccountEnabled(email)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findEnabledUserByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().isAccountEnabled()).isTrue();
        verify(userRepository).findByEmailIgnoreCaseAndAccountEnabled(email);
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserProfile_WhenUserExists_ShouldReturnUserResponse() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmailWithActiveAddresses(email)).thenReturn(Optional.of(testUser));

        // When
        UserResponse result = userService.getUserProfile(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(userRepository).findByEmailWithActiveAddresses(email);
    }

    @Test
    void getUserProfile_WhenUserNotExists_ShouldThrowException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmailWithActiveAddresses(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserProfile(email))
                .isInstanceOf(UserService.UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmailWithActiveAddresses(email);
    }

    @Test
    void updateUserProfile_WhenUserExists_ShouldUpdateAndReturnUser() {
        // Given
        String email = "test@example.com";
        UserService.UserUpdateRequest updateRequest = new UserService.UserUpdateRequest();
        updateRequest.setFirstName("Updated First Name");
        updateRequest.setLastName("Updated Last Name");
        updateRequest.setPhoneNumber("+1234567890");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.updateUserProfile(email, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findByEmailIgnoreCase(email);
        verify(userRepository).save(testUser);
    }

    @Test
    void lockUserAccount_WhenUserExists_ShouldLockAccount() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.lockUserAccount(email);

        // Then
        assertThat(testUser.isAccountLocked()).isTrue();
        verify(userRepository).findByEmailIgnoreCase(email);
        verify(userRepository).save(testUser);
    }

    @Test
    void unlockUserAccount_WhenUserExists_ShouldUnlockAccount() {
        // Given
        String email = "test@example.com";
        testUser.setAccountLocked(true);
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.unlockUserAccount(email);

        // Then
        assertThat(testUser.isAccountLocked()).isFalse();
        verify(userRepository).findByEmailIgnoreCase(email);
        verify(userRepository).save(testUser);
    }

    @Test
    void existsByEmail_WhenUserExists_ShouldReturnTrue() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(true);

        // When
        boolean result = userService.existsByEmail(email);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmailIgnoreCase(email);
    }

    @Test
    void getActiveUsersCount_ShouldReturnCount() {
        // Given
        long expectedCount = 100L;
        when(userRepository.countActiveUsers()).thenReturn(expectedCount);

        // When
        long result = userService.getActiveUsersCount();

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(userRepository).countActiveUsers();
    }

    // Helper methods
    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setPhoneNumber("+1234567890");
        user.setAccountEnabled(true);
        user.setEmailVerified(false);
        user.setAccountLocked(false);
        return user;
    }

    private SignupRequest createSignupRequest() {
        SignupRequest request = new SignupRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("Test@Example.com"); // Mixed case to test normalization
        request.setPassword("password123");
        request.setPhoneNumber("+1234567890");
        return request;
    }
}
