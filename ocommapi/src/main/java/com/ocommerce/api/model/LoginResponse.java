package com.ocommerce.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class LoginResponse {

    /** The JWT token to be used for authentication. */
    private String token;

}
