package com.ocommerce.services.cart.repository;


import com.ocommerce.services.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    java.util.Optional<Cart> findByUserId(UUID userId);
    // Add custom queries as needed
}
