package com.ocommerce.services.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the application.
 * These properties are loaded from application.properties and validated.
 */
@ConfigurationProperties(prefix = "app")
@Validated
public record ApplicationProperties(
        @NotBlank String name,
        @NotBlank String version,
        @NotBlank String description,
        @NotNull Security security) {

    public record Security(
            @NotNull Jwt jwt) {

        public record Jwt(
                @NotBlank String secret,
                @Positive Long accessTokenExpirationMs,
                @Positive Long refreshTokenExpirationMs) {
        }
    }
}
