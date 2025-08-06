package com.ocommerce.services.user.service;

import com.ocommerce.services.security.JwtUtil;
import com.ocommerce.services.user.domain.RefreshToken;
import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing refresh tokens
 */
@Service
@Transactional
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Create a new refresh token for user
     * 
     * @param user the user
     * @return created refresh token
     */
    public RefreshToken createRefreshToken(User user) {
        // Generate unique token
        String tokenValue = generateUniqueToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(jwtUtil.getRefreshTokenExpiration());

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        logger.info("Created refresh token for user: {}", user.getEmail());

        return savedToken;
    }

    /**
     * Find refresh token by token string
     * 
     * @param token token string
     * @return refresh token if found
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Find valid refresh token by token string
     * 
     * @param token token string
     * @return valid refresh token if found
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValidToken(String token) {
        return refreshTokenRepository.findValidByToken(token, LocalDateTime.now());
    }

    /**
     * Verify and get refresh token
     * 
     * @param token token string
     * @return valid refresh token
     * @throws InvalidRefreshTokenException if token is invalid
     */
    @Transactional(readOnly = true)
    public RefreshToken verifyToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (!refreshToken.isValid()) {
            logger.warn("Invalid refresh token used: expired or revoked");
            throw new InvalidRefreshTokenException("Refresh token is invalid");
        }

        return refreshToken;
    }

    /**
     * Revoke refresh token
     * 
     * @param token token string
     */
    public void revokeToken(String token) {
        refreshTokenRepository.revokeByToken(token);
        logger.info("Refresh token revoked");
    }

    /**
     * Revoke all refresh tokens for user
     * 
     * @param user the user
     */
    public void revokeAllTokensForUser(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        logger.info("All refresh tokens revoked for user: {}", user.getEmail());
    }

    /**
     * Delete token (hard delete)
     * 
     * @param refreshToken the token to delete
     */
    public void deleteToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
        logger.info("Refresh token deleted for user: {}", refreshToken.getUser().getEmail());
    }

    /**
     * Clean up expired tokens (scheduled job)
     */
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        logger.info("Cleaned up expired refresh tokens");
    }

    /**
     * Clean up revoked tokens (scheduled job)
     */
    public void cleanupRevokedTokens() {
        refreshTokenRepository.deleteRevokedTokens();
        logger.info("Cleaned up revoked refresh tokens");
    }

    /**
     * Count valid tokens for user
     * 
     * @param user the user
     * @return count of valid tokens
     */
    @Transactional(readOnly = true)
    public long countValidTokensForUser(User user) {
        return refreshTokenRepository.countValidByUser(user, LocalDateTime.now());
    }

    /**
     * Generate unique token string
     * 
     * @return unique token
     */
    private String generateUniqueToken() {
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (refreshTokenRepository.findByToken(token).isPresent());

        return token;
    }

    // Exception class
    public static class InvalidRefreshTokenException extends RuntimeException {
        public InvalidRefreshTokenException(String message) {
            super(message);
        }
    }
}
