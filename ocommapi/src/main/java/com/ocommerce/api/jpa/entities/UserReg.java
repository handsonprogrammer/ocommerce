package com.ocommerce.api.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * User for authentication with our website.
 */
@Entity
@Data
@Table(name = "userreg")
public class UserReg {

    /** Unique id for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;
    /** The username of the user. */
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    /** The encrypted password of the user. */
    @Column(name = "password", nullable = false, length = 1000)
    private String password;
    /** The email of the user. */
    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;
    /** The first name of the user. */
    @Column(name = "firstname", nullable = false)
    private String firstName;
    /** The last name of the user. */
    @Column(name = "lastname", nullable = false)
    private String lastName;
    /** Field to specify if password is expired. */
    @Column(columnDefinition = "boolean default false")
    private boolean isPasswordExpired =false;
    /** Field to specify if user is disabled. */
    @Column(columnDefinition = "boolean default true")
    private boolean isEnabled = true;
    /** Field to specify the number of wrong password entered continuously */
    @Column(columnDefinition = "integer default 0")
    private int passwordRetries;
    /** The addresses associated with the user. */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();
}
