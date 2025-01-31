package com.ocommerce.api.jpa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Address for the user to be billed/delivered to.
 */
@Entity
@Data
@Table(name = "address")
public class Address {

    /** Unique id for the address. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false)
    private Long addressId;
    /** The first line of address. */
    @Column(name = "addressline1", nullable = false, length = 512)
    private String addressLine1;
    /** The second line of address. */
    @Column(name = "addressline2", length = 512)
    private String addressLine2;
    /** The city of the address. */
    @Column(name = "city", nullable = false)
    private String city;
    /** The country of the address. */
    @Column(name = "country", nullable = false, length = 75)
    private String country;
    /** The country of the address. */
    @Column(name = "zipcode", nullable = false, length = 75)
    private String zipcode;
    /** The status of the address. T - deleted, A- active.*/
    @Column(name = "status", columnDefinition = "varchar(1) not null default 'P'")
    private char status;
    /** The user the address is associated with. */
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserReg user;

}
