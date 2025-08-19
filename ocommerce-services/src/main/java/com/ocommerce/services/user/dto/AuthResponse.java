package com.ocommerce.services.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Authentication response DTO containing JWT tokens and user info
 */
@Schema(description = "Authentication response with tokens and user information")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("accessToken")
    private String accessToken;

    @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("refreshToken")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @JsonProperty("tokenType")
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiration time in seconds", example = "3600")
    @JsonProperty("expiresIn")
    private long expiresIn;

    @Schema(description = "Token expiration timestamp")
    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;

    @Schema(description = "User information")
    @JsonProperty("user")
    private UserResponse user;

    public AuthResponse(String accessToken, @NotBlank(message = "Token is required") String token, long accessTokenExpirationSeconds) {
        this.accessToken = accessToken;
        this.refreshToken = token;
        this.expiresIn = accessTokenExpirationSeconds;
        this.expiresAt = LocalDateTime.now().plusSeconds(accessTokenExpirationSeconds);
    }
}
