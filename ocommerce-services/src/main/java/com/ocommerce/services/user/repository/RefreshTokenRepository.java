package com.ocommerce.services.user.repository;

import com.ocommerce.services.user.domain.RefreshToken;
import com.ocommerce.services.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for RefreshToken entity for JWT token management
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find refresh token by token string
     * 
     * @param token the token string
     * @return Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByUserAndToken(User user, String token);

    /**
     * Find valid (non-revoked and non-expired) refresh token by token string
     * 
     * @param token the token string
     * @return Optional containing the valid refresh token if found
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.revoked = false AND rt.expiryDate > :now")
    Optional<RefreshToken> findValidByToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Find all refresh tokens for a user
     * 
     * @param user the user
     * @return list of refresh tokens
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find all valid refresh tokens for a user
     * 
     * @param user the user
     * @return list of valid refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiryDate > :now")
    List<RefreshToken> findValidByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Revoke all refresh tokens for a user
     * 
     * @param user the user
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllByUser(@Param("user") User user);

    /**
     * Revoke specific refresh token
     * 
     * @param token the token string
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token AND rt.user = :user")
    void revokeByTokenAndUser(@Param("token") String token, @Param("user") User user);

    /**
     * Delete expired refresh tokens (cleanup job)
     * 
     * @param now current timestamp
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete all revoked refresh tokens (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true")
    void deleteRevokedTokens();

    /**
     * Count valid refresh tokens for a user
     * 
     * @param user the user
     * @return count of valid tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiryDate > :now")
    long countValidByUser(@Param("user") User user, @Param("now") LocalDateTime now);
}
