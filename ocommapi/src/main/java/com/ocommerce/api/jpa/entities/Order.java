package com.ocommerce.api.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

import com.ocommerce.api.constants.OrderStatus;
import com.ocommerce.api.exception.OrderLockedException;

/**
 * Order generated from the website.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "orders")
public class Order extends BaseOrderEntity {

    /** Unique id for the order. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** The status of the order. */
    @Column(name = "status", columnDefinition = "varchar(1) not null default 'P'")
    private OrderStatus status = OrderStatus.PENDING; // Default status is PENDING

    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<OrderItems> orderItems = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (isLocked()) {
            throw new OrderLockedException("Cannot persist a locked cart.");
        }
    }

}
