package com.ocommerce.services.user.domain;

import com.ocommerce.services.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Refresh token entity for JWT authentication.
 * Stores refresh tokens securely in the database for revocation and lifecycle
 * management.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"token", "user"}) // Exclude sensitive token and user to avoid circular references
@EqualsAndHashCode(callSuper = true, exclude = "user") // Exclude user to avoid circular references
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

    // Custom constructor
    public RefreshToken(String token, LocalDateTime expiryDate, User user) {
        super();
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
}
