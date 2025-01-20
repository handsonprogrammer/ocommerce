package com.ocommerce.api.jpa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Order generated from the website.
 */
@Entity
@Data
@Table(name = "orders")
public class Order {

    /** Unique id for the order. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    /** The user of the order. */
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserReg user;
    /** The shipping address of the order. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "shippingaddress_id", nullable = false)
    private Address shippingAddress;
    /** The Billing address of the order. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "billingaddress_id", nullable = false)
    private Address billingAddress;
    /** The quantities ordered. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<OrderItems> quantities = new ArrayList<>();
}
