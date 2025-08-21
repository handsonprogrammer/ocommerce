package com.ocommerce.services.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = true)
    private UUID variantId;

    @Column(nullable = false)
    private Integer quantity;

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

    // Calculate total price for this order item (if totalPrice not set)
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
