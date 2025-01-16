package com.ocommerce.api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * The information required to register a user.
 */
@Getter
public class RegistrationBody {

    /** The username. */
    @NotNull
    @NotBlank
    @Size(min=6, max=255)
    private String username;
    /** The email. */
    @NotNull
    @NotBlank
    @Email
    private String email;
    /** The password. */
    @NotNull
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
    @Size(min=8, max=32)
    private String password;
    /** The first name. */
    @NotNull
    @NotBlank
    private String firstName;
    /** The last name. */
    @NotNull
    @NotBlank
    private String lastName;
}
