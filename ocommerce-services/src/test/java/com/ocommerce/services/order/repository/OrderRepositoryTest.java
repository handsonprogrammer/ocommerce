package com.ocommerce.services.order.repository;

import com.ocommerce.services.order.domain.Order;
import com.ocommerce.services.order.domain.OrderItem;
import com.ocommerce.services.order.domain.OrderStatus;
import com.ocommerce.services.order.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    private UUID userId;
    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        // Create first order
        order1 = new Order();
        order1.setUserId(userId);
        order1.setShippingAddressId(UUID.randomUUID());
        order1.setBillingAddressId(UUID.randomUUID());
        order1.setOrderStatus(OrderStatus.PENDING);
        order1.setPaymentStatus(PaymentStatus.PENDING);
        order1.setTotalAmount(BigDecimal.valueOf(200));
        
        OrderItem item1 = new OrderItem();
        item1.setOrder(order1);
        item1.setProductId(UUID.randomUUID());
        item1.setQuantity(2);
        item1.setUnitPrice(BigDecimal.valueOf(100));
        order1.setItems(List.of(item1));
        
        // Create second order
        order2 = new Order();
        order2.setUserId(userId);
        order2.setShippingAddressId(UUID.randomUUID());
        order2.setBillingAddressId(UUID.randomUUID());
        order2.setOrderStatus(OrderStatus.CONFIRMED);
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        order2.setTotalAmount(BigDecimal.valueOf(150));
        
        OrderItem item2 = new OrderItem();
        item2.setOrder(order2);
        item2.setProductId(UUID.randomUUID());
        item2.setQuantity(1);
        item2.setUnitPrice(BigDecimal.valueOf(150));
        order2.setItems(List.of(item2));
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_shouldReturnOrdersInDescendingOrder() {
        // Given
        Order savedOrder1 = orderRepository.save(order1);
        Order savedOrder2 = orderRepository.save(order2);
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Order> result = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getCreatedAt())
            .isAfterOrEqualTo(result.getContent().get(1).getCreatedAt());
    }

    @Test
    void findByUserIdAndOrderStatus_shouldReturnOrdersWithSpecificStatus() {
        // Given
        orderRepository.save(order1);
        orderRepository.save(order2);
        
        // When
        List<Order> pendingOrders = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.PENDING);
        List<Order> confirmedOrders = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.CONFIRMED);
        
        // Then
        assertThat(pendingOrders).hasSize(1);
        assertThat(pendingOrders.get(0).getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(confirmedOrders).hasSize(1);
        assertThat(confirmedOrders.get(0).getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void findByIdAndUserId_shouldReturnOrderWhenUserOwnsIt() {
        // Given
        Order savedOrder = orderRepository.save(order1);
        
        // When
        Optional<Order> result = orderRepository.findByIdAndUserId(savedOrder.getId(), userId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedOrder.getId());
        assertThat(result.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void findByIdAndUserId_shouldReturnEmptyWhenUserDoesNotOwnOrder() {
        // Given
        Order savedOrder = orderRepository.save(order1);
        UUID differentUserId = UUID.randomUUID();
        
        // When
        Optional<Order> result = orderRepository.findByIdAndUserId(savedOrder.getId(), differentUserId);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void countByUserId_shouldReturnCorrectCount() {
        // Given
        orderRepository.save(order1);
        orderRepository.save(order2);
        
        // When
        long count = orderRepository.countByUserId(userId);
        
        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void findRecentOrdersByUserId_shouldReturnLimitedResults() {
        // Given
        orderRepository.save(order1);
        orderRepository.save(order2);
        Pageable pageable = PageRequest.of(0, 1);
        
        // When
        List<Order> result = orderRepository.findRecentOrdersByUserId(userId, pageable);
        
        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void save_shouldPersistOrderWithItems() {
        // When
        Order savedOrder = orderRepository.save(order1);
        
        // Then
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getUserId()).isEqualTo(userId);
        assertThat(savedOrder.getItems()).hasSize(1);
        assertThat(savedOrder.getItems().get(0).getId()).isNotNull();
        assertThat(savedOrder.getCreatedAt()).isNotNull();
        assertThat(savedOrder.getUpdatedAt()).isNotNull();
    }

    @Test
    void calculateTotalAmount_shouldReturnCorrectTotal() {
        // Given
        Order savedOrder = orderRepository.save(order1);
        
        // When
        BigDecimal totalAmount = savedOrder.calculateTotalAmount();
        
        // Then
        assertThat(totalAmount).isEqualTo(BigDecimal.valueOf(200)); // 2 * 100
    }
}
