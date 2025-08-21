package com.ocommerce.services.cart.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter
@Setter
public class Cart {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items;

    @Column(nullable = true)
    private UUID shippingAddressId;

    @Column(nullable = true)
    private UUID billingAddressId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Helper method to calculate total cart amount
    public java.math.BigDecimal getTotalAmount() {
        if (items == null || items.isEmpty()) return java.math.BigDecimal.ZERO;
        return items.stream()
            .map(CartItem::getTotalPrice)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    // Getters and setters
}
