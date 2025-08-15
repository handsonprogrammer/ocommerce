package com.ocommerce.services.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for user response (never expose entities directly in controller
 * responses)
 */
@Schema(description = "User information response")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("id")
    private UUID id;

    @Schema(description = "User's first name", example = "John")
    @JsonProperty("firstName")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    @JsonProperty("lastName")
    private String lastName;

    @Schema(description = "User's full name", example = "John Doe")
    @JsonProperty("fullName")
    private String fullName;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    @JsonProperty("email")
    private String email;

    @Schema(description = "User's phone number", example = "+1234567890")
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @Schema(description = "Whether email is verified", example = "true")
    @JsonProperty("emailVerified")
    private boolean emailVerified;

    @Schema(description = "Whether account is enabled", example = "true")
    @JsonProperty("accountEnabled")
    private boolean accountEnabled;

    @Schema(description = "Account creation timestamp")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @Schema(description = "User's addresses")
    @JsonProperty("addresses")
    private List<AddressResponse> addresses;
}
