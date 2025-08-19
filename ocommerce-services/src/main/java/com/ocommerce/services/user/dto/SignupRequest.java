package com.ocommerce.services.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for user signup request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request")
public class SignupRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Schema(description = "User's first name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("firstName")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Schema(description = "User's last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("lastName")
    private String lastName;


    /*
         The following restrictions are imposed in the email address' local part by using this regex:

            - It allows numeric values from 0 to 9.
            - Both uppercase and lowercase letters from a to z are allowed.
            - Allowed are underscore "_", hyphen "-", and dot ".".
            - Dot isn't allowed at the start and end of the local part.
            - Consecutive dots aren't allowed.
            - For the local part, a maximum of 64 characters are allowed.

        Restrictions for the domain part in this regular expression include:

            - It allows numeric values from 0 to 9.
            - We allow both uppercase and lowercase letters from a to z.
            - Hyphen "-" and dot "." aren't allowed at the start and end of the domain part.
            - No consecutive dots.
    */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid", regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    @Schema(description = "User's email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "User's password", example = "securePassword123@", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    @JsonProperty("password")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^!&+=])(?=\\S+$).{8,20}$",
             message = "Password must be 8-20 characters long, contain at least one digit, one lowercase letter, one uppercase letter, and one special character (@#$%^!&+=), and no whitespace")
    private String password;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Schema(description = "User's phone number", example = "+1234567890")
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @Override
    public String toString() {
        return "SignupRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
