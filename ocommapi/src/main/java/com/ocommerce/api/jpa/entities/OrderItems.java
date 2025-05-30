package com.ocommerce.api.jpa.entities;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * The quantity ordered of a product.
 */
@Entity
@Data
@Table(name = "orderitems")
public class OrderItems {

    /** The unqiue id of the order quantity. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderitems_id", nullable = false)
    private Long orderItems_Id;
    /** The product being ordered. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    /** The quantity being ordered. */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    /** The order itself. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    /** The price of the product at the time of order. */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;
    /** The total price of the order items. */
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;
    /** The tax applied to the order items. */
    @Column(name = "tax", nullable = false, precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;
    /** The shipping cost applied to the order items. */
    @Column(name = "shipping_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;
    /** The total amount of the order items. */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    /** The discount applied to the order items. */
    @Column(name = "discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;
    /** The user who placed the order. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserReg user;

    @Column
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date editedAt;
}
