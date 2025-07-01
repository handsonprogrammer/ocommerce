package com.ocommerce.api.jpa.entities;

import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ocommerce.api.constants.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@MappedSuperclass
public abstract class BaseOrderEntity {

    /** The user of the order. */
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserReg user;
    /** The shipping address of the order. */
    @ManyToOne
    @JoinColumn(name = "shippingaddress_id", nullable = true)
    private Address shippingAddress;
    /** The Billing address of the order. */
    @ManyToOne
    @JoinColumn(name = "billingaddress_id", nullable = true)
    private Address billingAddress;
    /** The total amount of the order. */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    /** The total tax of the order. */
    @Column(name = "total_tax", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalTax = BigDecimal.ZERO;
    /** The total shipping cost of the order. */
    @Column(name = "total_shipping_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalShippingCost = BigDecimal.ZERO;
    /** field to check if order is locked */
    @Column(name = "locked", columnDefinition = "boolean default false")
    private boolean isLocked = false;

    @Column
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public UserReg getUser() {
        return user;
    }

    public void setUser(UserReg user) {
        this.user = user;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(BigDecimal totalTax) {
        this.totalTax = totalTax;
    }

    public BigDecimal getTotalShippingCost() {
        return totalShippingCost;
    }

    public void setTotalShippingCost(BigDecimal totalShippingCost) {
        this.totalShippingCost = totalShippingCost;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

}
