package com.ocommerce.services.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user profile update request
 */
@Schema(description = "User profile update request")
public class UserUpdateRequest {

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Schema(description = "User's first name", example = "John")
    @JsonProperty("firstName")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Schema(description = "User's last name", example = "Doe")
    @JsonProperty("lastName")
    private String lastName;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Schema(description = "User's phone number", example = "+1234567890")
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    // Constructors
    public UserUpdateRequest() {
    }

    // Getters and setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
