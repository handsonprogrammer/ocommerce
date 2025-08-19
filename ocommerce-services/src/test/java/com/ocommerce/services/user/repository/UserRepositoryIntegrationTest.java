package com.ocommerce.services.user.repository;

import com.ocommerce.services.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository using TestContainers
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaAuditing
@ActiveProfiles("integration-test")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
    }

    @Test
    void findByEmailIgnoreCase_WhenUserExists_ShouldReturnUser() {
        // Given
        User savedUser = userRepository.save(testUser);

        // When
        Optional<User> result = userRepository.findByEmailIgnoreCase("TEST@EXAMPLE.COM");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmailIgnoreCase_WhenUserNotExists_ShouldReturnEmpty() {
        // When
        Optional<User> result = userRepository.findByEmailIgnoreCase("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmailIgnoreCase_WhenUserExists_ShouldReturnTrue() {
        // Given
        userRepository.save(testUser);

        // When
        boolean result = userRepository.existsByEmailIgnoreCase("TEST@EXAMPLE.COM");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByEmailIgnoreCase_WhenUserNotExists_ShouldReturnFalse() {
        // When
        boolean result = userRepository.existsByEmailIgnoreCase("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void findByEmailIgnoreCaseAndAccountEnabled_WhenUserEnabledExists_ShouldReturnUser() {
        // Given
        testUser.setAccountEnabled(true);
        userRepository.save(testUser);

        // When
        Optional<User> result = userRepository.findByEmailIgnoreCaseAndAccountEnabled("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().isAccountEnabled()).isTrue();
    }

    @Test
    void findByEmailIgnoreCaseAndAccountEnabled_WhenUserDisabled_ShouldReturnEmpty() {
        // Given
        testUser.setAccountEnabled(false);
        userRepository.save(testUser);

        // When
        Optional<User> result = userRepository.findByEmailIgnoreCaseAndAccountEnabled("test@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmailWithActiveAddresses_WhenUserExists_ShouldReturnUser() {
        // Given
        User savedUser = userRepository.save(testUser);

        // When
        Optional<User> result = userRepository.findByEmailWithActiveAddresses("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void countActiveUsers_WhenActiveUsersExist_ShouldReturnCount() {
        // Given

        User user1 = createTestUser();
        user1.setEmail("user1@example.com");
        user1.setAccountEnabled(true);
        user1.setAccountLocked(false);

        User user2 = createTestUser();
        user2.setEmail("user2@example.com");
        user2.setAccountEnabled(true);
        user2.setAccountLocked(false);

        User disabledUser = createTestUser();
        disabledUser.setEmail("disabled@example.com");
        disabledUser.setAccountEnabled(false);
        disabledUser.setAccountLocked(false);

        User deletedUser = createTestUser();
        deletedUser.setEmail("deleted@example.com");
        deletedUser.setAccountEnabled(true);
        deletedUser.setAccountLocked(true);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(disabledUser);
        userRepository.save(deletedUser);

        // When
        long count = userRepository.countActiveUsers();

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void save_WhenNewUser_ShouldGenerateIdAndTimestamps() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void update_WhenExistingUser_ShouldUpdateTimestamp() {
        // Given
        User savedUser = userRepository.save(testUser);
        String originalFirstName = savedUser.getFirstName();

        // When
        savedUser.setFirstName("Updated Name");
        User updatedUser = userRepository.saveAndFlush(savedUser);

        // Then
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getUpdatedAt()).isAfter(updatedUser.getCreatedAt());
        assertThat(updatedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void softDelete_WhenUserDeleted_ShouldMarkAsDeleted() {
        // Given
        User savedUser = userRepository.save(testUser);

        // When
        savedUser.setDeleted(true);
        userRepository.save(savedUser);

        // Then
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().isDeleted()).isTrue();
    }

    // Helper methods
    private User createTestUser() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setPhoneNumber("+1234567890");
        user.setAccountEnabled(true);
        user.setEmailVerified(false);
        user.setAccountLocked(false);
        user.setDeleted(false);
        return user;
    }
}
