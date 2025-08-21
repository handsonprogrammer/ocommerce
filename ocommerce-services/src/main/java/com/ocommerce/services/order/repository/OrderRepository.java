package com.ocommerce.services.order.repository;

import com.ocommerce.services.order.domain.Order;
import com.ocommerce.services.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Find orders by user ID with pagination
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // Find orders by user ID and status
    List<Order> findByUserIdAndOrderStatus(UUID userId, OrderStatus orderStatus);

    // Find order by ID and user ID (for security)
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    // Count orders by user ID
    long countByUserId(UUID userId);

    // Find recent orders for user
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUserId(@Param("userId") UUID userId, Pageable pageable);
}
