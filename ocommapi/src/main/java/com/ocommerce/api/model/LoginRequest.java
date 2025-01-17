package com.ocommerce.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LoginRequest {

    /** The username to log in with. */
    @NotNull
    @NotBlank
    private String username;
    /** The password to log in with. */
    @NotNull
    @NotBlank
    private String password;

}
