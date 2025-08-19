package com.ocommerce.services.user.repository;

import com.ocommerce.services.user.domain.RefreshToken;
import com.ocommerce.services.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RefreshTokenRepository using TestContainers
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableJpaAuditing
class RefreshTokenRepositoryIT {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private RefreshToken validToken;
    private RefreshToken expiredToken;
    private RefreshToken revokedToken;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testUser = userRepository.save(testUser);

        validToken = createValidRefreshToken();
        expiredToken = createExpiredRefreshToken();
        revokedToken = createRevokedRefreshToken();

        refreshTokenRepository.save(validToken);
        refreshTokenRepository.save(expiredToken);
        refreshTokenRepository.save(revokedToken);
        refreshTokenRepository.flush();
    }

    @Test
    void findByUserAndToken_WhenTokenExists_ShouldReturnToken() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByUserAndToken(testUser, "valid-token");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("valid-token");
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findByUserAndToken_WhenTokenNotExists_ShouldReturnEmpty() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByUserAndToken(testUser, "non-existent-token");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findValidByToken_WhenTokenValidAndNotExpired_ShouldReturnToken() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findValidByToken("valid-token", LocalDateTime.now());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("valid-token");
        assertThat(result.get().isRevoked()).isFalse();
    }

    @Test
    void findValidByToken_WhenTokenExpired_ShouldReturnEmpty() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findValidByToken("expired-token", LocalDateTime.now());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findValidByToken_WhenTokenRevoked_ShouldReturnEmpty() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findValidByToken("revoked-token", LocalDateTime.now());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_WhenUserHasTokens_ShouldReturnAllTokens() {
        // When
        List<RefreshToken> result = refreshTokenRepository.findByUser(testUser);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(RefreshToken::getToken)
                .containsExactlyInAnyOrder("valid-token", "expired-token", "revoked-token");
    }

    @Test
    void findValidByUser_WhenUserHasValidTokens_ShouldReturnOnlyValidTokens() {
        // When
        List<RefreshToken> result = refreshTokenRepository.findValidByUser(testUser, LocalDateTime.now());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getToken()).isEqualTo("valid-token");
        assertThat(result.get(0).isRevoked()).isFalse();
    }

    @Test
    void revokeAllByUser_WhenUserHasTokens_ShouldRevokeAllTokens() {
        // When
        refreshTokenRepository.revokeAllByUser(testUser);
        refreshTokenRepository.flush();

        // Then
        List<RefreshToken> tokens = refreshTokenRepository.findByUser(testUser);
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allMatch(RefreshToken::isRevoked);
    }

    @Test
    void revokeByTokenAndUser_WhenTokenExists_ShouldRevokeSpecificToken() {
        // When
        refreshTokenRepository.revokeByTokenAndUser("valid-token", testUser);
        refreshTokenRepository.flush();

        // Then
        Optional<RefreshToken> revokedToken = refreshTokenRepository.findByUserAndToken(testUser, "valid-token");
        assertThat(revokedToken).isPresent();
        assertThat(revokedToken.get().isRevoked()).isTrue();

        // Other tokens should remain unchanged
        Optional<RefreshToken> otherToken = refreshTokenRepository.findByUserAndToken(testUser, "expired-token");
        assertThat(otherToken).isPresent();
        assertThat(otherToken.get().isRevoked()).isFalse();
    }

    @Test
    void deleteExpiredTokens_WhenExpiredTokensExist_ShouldDeleteOnlyExpiredTokens() {
        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        refreshTokenRepository.flush();

        // Then
        List<RefreshToken> remainingTokens = refreshTokenRepository.findByUser(testUser);
        assertThat(remainingTokens).hasSize(2);
        assertThat(remainingTokens).extracting(RefreshToken::getToken)
                .containsExactlyInAnyOrder("valid-token", "revoked-token");
    }

    @Test
    void deleteRevokedTokens_WhenRevokedTokensExist_ShouldDeleteOnlyRevokedTokens() {
        // When
        refreshTokenRepository.deleteRevokedTokens();
        refreshTokenRepository.flush();

        // Then
        List<RefreshToken> remainingTokens = refreshTokenRepository.findByUser(testUser);
        assertThat(remainingTokens).hasSize(2);
        assertThat(remainingTokens).extracting(RefreshToken::getToken)
                .containsExactlyInAnyOrder("valid-token", "expired-token");
    }

    @Test
    void countValidByUser_WhenUserHasValidTokens_ShouldReturnCorrectCount() {
        // When
        long count = refreshTokenRepository.countValidByUser(testUser, LocalDateTime.now());

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void countValidByUser_WhenUserHasNoValidTokens_ShouldReturnZero() {
        // Given - revoke all tokens
        refreshTokenRepository.revokeAllByUser(testUser);
        refreshTokenRepository.flush();

        // When
        long count = refreshTokenRepository.countValidByUser(testUser, LocalDateTime.now());

        // Then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void save_WhenNewToken_ShouldGenerateIdAndTimestamps() {
        // Given
        RefreshToken newToken = new RefreshToken();
        newToken.setUser(testUser);
        newToken.setToken("new-token");
        newToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        newToken.setRevoked(false);

        // When
        RefreshToken savedToken = refreshTokenRepository.save(newToken);

        // Then
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getCreatedAt()).isNotNull();
        assertThat(savedToken.getUpdatedAt()).isNotNull();
    }

    // Helper methods
    private User createTestUser() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("refreshtoken@example.com");
        user.setPassword("encoded-password");
        user.setPhoneNumber("+1234567890");
        user.setAccountEnabled(true);
        user.setEmailVerified(false);
        user.setAccountLocked(false);
        user.setDeleted(false);
        return user;
    }

    private RefreshToken createValidRefreshToken() {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID());
        token.setUser(testUser);
        token.setToken("valid-token");
        token.setExpiryDate(LocalDateTime.now().plusDays(7));
        token.setRevoked(false);
        return token;
    }

    private RefreshToken createExpiredRefreshToken() {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID());
        token.setUser(testUser);
        token.setToken("expired-token");
        token.setExpiryDate(LocalDateTime.now().minusDays(1));
        token.setRevoked(false);
        return token;
    }

    private RefreshToken createRevokedRefreshToken() {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID());
        token.setUser(testUser);
        token.setToken("revoked-token");
        token.setExpiryDate(LocalDateTime.now().plusDays(7));
        token.setRevoked(true);
        return token;
    }
}

