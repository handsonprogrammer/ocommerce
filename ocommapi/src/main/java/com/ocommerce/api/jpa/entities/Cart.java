package com.ocommerce.api.jpa.entities;

import java.util.ArrayList;
import java.util.List;

import com.ocommerce.api.constants.CartStatus;
import com.ocommerce.api.constants.OrderStatus;
import com.ocommerce.api.exception.CartLockedException;

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

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "cart")
public class Cart extends BaseOrderEntity {

    /** Unique id for the order. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    /** The status of the order. */
    @Column(name = "status", columnDefinition = "varchar(1) not null default 'I'")
    private CartStatus status = CartStatus.INACTIVE; // Default status is ACTIVE

    /** The quantities ordered. */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (isLocked()) {
            throw new CartLockedException("Cannot persist a locked cart.");
        }
    }

}
