package com.ocommerce.services.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for user response (never expose entities directly in controller
 * responses)
 */
@Schema(description = "User information response")
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

    @Schema(description = "Account last update timestamp")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @Schema(description = "User's addresses")
    @JsonProperty("addresses")
    private List<AddressResponse> addresses;

    // Constructors
    public UserResponse() {
    }

    public UserResponse(UUID id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.email = email;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateFullName();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateFullName();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isAccountEnabled() {
        return accountEnabled;
    }

    public void setAccountEnabled(boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<AddressResponse> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressResponse> addresses) {
        this.addresses = addresses;
    }

    private void updateFullName() {
        if (firstName != null && lastName != null) {
            this.fullName = firstName + " " + lastName;
        }
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", emailVerified=" + emailVerified +
                ", accountEnabled=" + accountEnabled +
                '}';
    }
}
