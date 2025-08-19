package com.ocommerce.services.user.service;

import com.ocommerce.services.security.JwtUtil;
import com.ocommerce.services.user.domain.RefreshToken;
import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing refresh tokens
 */
@Slf4j
@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Create a new refresh token for the specified user
     * Generates a unique UUID-based token with expiration date from JwtUtil
     * 
     * @param user the user for whom to create the refresh token
     * @return the created and saved refresh token
     */
    public RefreshToken createRefreshToken(User user) {
        // Generate unique token
        String tokenValue = generateUniqueToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(jwtUtil.getRefreshTokenExpiration());

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}", user.getEmail());

        return savedToken;
    }

    /**
     * Find refresh token by user and token string
     * 
     * @param user  the user who owns the token
     * @param token the token string to search for
     * @return optional containing the refresh token if found
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByUserAndToken(User user, String token) {
        return refreshTokenRepository.findByUserAndToken(user, token);
    }

    /**
     * Find valid (non-expired, non-revoked) refresh token by token string
     * 
     * @param token the token string to search for
     * @return optional containing the valid refresh token if found and not expired
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValidToken(String token) {
        return refreshTokenRepository.findValidByToken(token, LocalDateTime.now());
    }

    /**
     * Verify and get refresh token for a specific user
     * Validates token exists, belongs to user, and is still valid (not expired or
     * revoked)
     * 
     * @param token the token string to verify
     * @param user  the user who should own the token
     * @return the valid refresh token
     * @throws InvalidRefreshTokenException if token is not found, doesn't belong to
     *                                      user, or is invalid
     */
    @Transactional(readOnly = true)
    public RefreshToken verifyTokenForUser(String token, User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserAndToken(user, token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (!refreshToken.isValid()) {
            log.warn("Invalid refresh token used: expired or revoked");
            throw new InvalidRefreshTokenException("Refresh token is invalid");
        }

        return refreshToken;
    }

    /**
     * Revoke a specific refresh token for a user
     * Marks the token as revoked in the database
     * 
     * @param token the token string to revoke
     * @param user  the user who owns the token
     */
    public void revokeTokenForUser(String token, User user) {
        refreshTokenRepository.revokeByTokenAndUser(token, user);
        log.info("Refresh token revoked for user: {}", user.getId());
    }

    /**
     * Revoke all refresh tokens for a specific user
     * Marks all tokens belonging to the user as revoked in the database
     * Useful for logout from all devices or security-related token invalidation
     * 
     * @param user the user whose tokens should be revoked
     */
    public void revokeAllTokensForUser(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        log.info("All refresh tokens revoked for user: {}", user.getEmail());
    }

    /**
     * Physically delete a refresh token from the database (hard delete)
     * Use this for immediate token removal, as opposed to soft deletion via
     * revocation
     * 
     * @param refreshToken the refresh token entity to delete
     */
    public void deleteToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
        log.info("Refresh token deleted for user: {}", refreshToken.getUser().getEmail());
    }

    /**
     * Clean up expired tokens from the database (scheduled job)
     * Removes all tokens that have passed their expiry date
     * Typically called by a scheduled task for database maintenance
     */
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up expired refresh tokens");
    }

    /**
     * Clean up revoked tokens from the database (scheduled job)
     * Removes all tokens that have been marked as revoked
     * Typically called by a scheduled task for database maintenance
     */
    public void cleanupRevokedTokens() {
        refreshTokenRepository.deleteRevokedTokens();
        log.info("Cleaned up revoked refresh tokens");
    }

    /**
     * Count the number of valid (non-expired, non-revoked) tokens for a specific
     * user
     * Useful for implementing token limits per user or monitoring active sessions
     * 
     * @param user the user whose valid tokens should be counted
     * @return the count of valid refresh tokens for the user
     */
    @Transactional(readOnly = true)
    public long countValidTokensForUser(User user) {
        return refreshTokenRepository.countValidByUser(user, LocalDateTime.now());
    }

    /**
     * Generate a unique token string using UUID
     * Ensures uniqueness by checking against existing tokens in the database
     * 
     * @return a unique UUID-based token string
     */
    private String generateUniqueToken() {
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (refreshTokenRepository.findValidByToken(token, LocalDateTime.now()).isPresent());

        return token;
    }

    // Exception class
    public static class InvalidRefreshTokenException extends RuntimeException {
        public InvalidRefreshTokenException(String message) {
            super(message);
        }
    }
}
