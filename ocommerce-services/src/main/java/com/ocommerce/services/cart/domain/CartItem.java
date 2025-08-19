package com.ocommerce.services.cart.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
public class CartItem {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = true)
    private UUID variantId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = true)
    private BigDecimal discountAmount;

    @Column(nullable = true)
    private BigDecimal taxAmount;

    // Additional fields for validated product information
    @Column(nullable = true)
    private String productName;

    @Column(nullable = true)
    private String variantName;

    @Column(nullable = true)
    private String sku;

    @Column(nullable = true)
    private BigDecimal totalPrice;

    // Helper method to calculate total price for this item (if totalPrice not set)
    public BigDecimal getTotalPrice() {
        if (totalPrice != null) {
            return totalPrice;
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        return subtotal.subtract(discount).add(tax);
    }
}
