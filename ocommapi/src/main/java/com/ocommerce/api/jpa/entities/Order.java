package com.ocommerce.api.jpa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ocommerce.api.constants.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    /** The status of the order. */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", columnDefinition = "varchar(1) not null default 'P'")
    private OrderStatus status = OrderStatus.PENDING; // Default status is PENDING
    /** The total amount of the order. */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    /** The total tax of the order. */
    @Column(name = "total_tax", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalTax = BigDecimal.ZERO;
    /** The total shipping cost of the order. */
    @Column(name = "total_shipping_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalShippingCost = BigDecimal.ZERO;
    /** The quantities ordered. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<OrderItems> orderItems = new ArrayList<>();

    @Column
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date editedAt;
}
