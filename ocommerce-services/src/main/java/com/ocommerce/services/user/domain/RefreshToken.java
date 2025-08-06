package com.ocommerce.services.user.domain;

import com.ocommerce.services.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Refresh token entity for JWT authentication.
 * Stores refresh tokens securely in the database for revocation and lifecycle
 * management.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    @NotBlank(message = "Token is required")
    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @NotNull(message = "Expiry date is required")
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    // Constructors
    public RefreshToken() {
        super();
    }

    public RefreshToken(String token, LocalDateTime expiryDate, User user) {
        this();
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
    }

    // Business methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public void revoke() {
        this.revoked = true;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), token);
    }

    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + getId() +
                ", token='" + token.substring(0, Math.min(token.length(), 10)) + "...' " +
                ", expiryDate=" + expiryDate +
                ", revoked=" + revoked +
                '}';
    }
}
