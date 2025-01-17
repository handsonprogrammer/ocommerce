package com.ocommerce.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetails {

    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

}
