package com.ocommerce.services.order.service;

import com.ocommerce.services.cart.domain.Cart;
import com.ocommerce.services.cart.domain.CartItem;
import com.ocommerce.services.cart.dto.ProductPricingInfo;
import com.ocommerce.services.cart.repository.CartRepository;
import com.ocommerce.services.cart.service.ProductValidationService;
import com.ocommerce.services.order.domain.Order;
import com.ocommerce.services.order.domain.OrderStatus;
import com.ocommerce.services.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductValidationService productValidationService;

    @InjectMocks
    private OrderService orderService;

    private UUID userId;
    private UUID productId;
    private UUID variantId;
    private UUID shippingAddressId;
    private UUID billingAddressId;
    private Cart cart;
    private CartItem cartItem;
    private Order order;
    private ProductPricingInfo pricingInfo;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        variantId = UUID.randomUUID();
        shippingAddressId = UUID.randomUUID();
        billingAddressId = UUID.randomUUID();

        cartItem = new CartItem();
        cartItem.setId(UUID.randomUUID());
        cartItem.setProductId(productId);
        cartItem.setVariantId(variantId);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(BigDecimal.valueOf(100));

        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUserId(userId);
        // Use mutable ArrayList instead of immutable List.of()
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        order = new Order();
        order.setId(UUID.randomUUID());
        order.setUserId(userId);
        order.setShippingAddressId(shippingAddressId);
        order.setBillingAddressId(billingAddressId);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(200));

        // Setup ProductPricingInfo
        pricingInfo = ProductPricingInfo.builder()
            .productId(productId)
            .variantId(variantId)
            .productName("Test Product")
            .variantName("Test Variant")
            .sku("TEST-SKU")
            .price(BigDecimal.valueOf(100))
            .unitOfMeasure("piece")
            .inventoryTracking(true)
            .availableStock(10)
            .isActive(true)
            .build();
    }

    @Test
    void createOrderFromCart_shouldCreateOrderSuccessfully() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Mock product validation for secure pricing
        when(productValidationService.validateAndGetProductPricing(eq(productId), eq(variantId)))
            .thenReturn(pricingInfo);
        when(productValidationService.validateStockAvailability(eq(productId), eq(variantId), eq(2)))
            .thenReturn(true);

        // When
        Order result = orderService.createOrderFromCart(userId, shippingAddressId, billingAddressId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getShippingAddressId()).isEqualTo(shippingAddressId);
        assertThat(result.getBillingAddressId()).isEqualTo(billingAddressId);
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(cart); // Cart should be cleared
        verify(productValidationService, times(2)).validateAndGetProductPricing(productId, variantId);
        verify(productValidationService).validateStockAvailability(productId, variantId, 2);
    }

    @Test
    void createOrderFromCart_shouldThrowExceptionIfCartNotFound() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrderFromCart(userId, shippingAddressId, billingAddressId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cart not found for user");
    }

    @Test
    void createOrderFromCart_shouldThrowExceptionIfCartIsEmpty() {
        // Given
        cart.setItems(new ArrayList<>());
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrderFromCart(userId, shippingAddressId, billingAddressId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cannot create order from empty cart");
    }

    @Test
    void createOrderFromCart_shouldThrowExceptionIfProductValidationFails() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productValidationService.validateAndGetProductPricing(eq(productId), eq(variantId)))
            .thenThrow(new RuntimeException("Product not found"));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrderFromCart(userId, shippingAddressId, billingAddressId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Product not found");
    }

    @Test
    void createOrderFromCart_shouldThrowExceptionIfInsufficientStock() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productValidationService.validateAndGetProductPricing(eq(productId), eq(variantId)))
            .thenReturn(pricingInfo);
        when(productValidationService.validateStockAvailability(eq(productId), eq(variantId), eq(2)))
            .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrderFromCart(userId, shippingAddressId, billingAddressId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Order validation failed")
            .hasMessageContaining("Insufficient stock");
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        // Given
        UUID orderId = order.getId();
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        // When
        Optional<Order> result = orderService.getOrderById(orderId, userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(order);
    }

    @Test
    void getUserOrders_shouldReturnPagedOrders() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> expectedPage = new PageImpl<>(List.of(order));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(expectedPage);

        // When
        Page<Order> result = orderService.getUserOrders(userId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(order);
    }

    @Test
    void cancelOrder_shouldCancelOrderSuccessfully() {
        // Given
        UUID orderId = order.getId();
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.cancelOrder(orderId, userId);

        // Then
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_shouldThrowExceptionIfOrderNotFound() {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    void cancelOrder_shouldThrowExceptionIfOrderAlreadyDelivered() {
        // Given
        UUID orderId = order.getId();
        order.setOrderStatus(OrderStatus.DELIVERED);
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cannot cancel order in status");
    }

    @Test
    void updateOrderStatus_shouldUpdateStatusSuccessfully() {
        // Given
        UUID orderId = order.getId();
        OrderStatus newStatus = OrderStatus.CONFIRMED;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.updateOrderStatus(orderId, newStatus);

        // Then
        assertThat(result.getOrderStatus()).isEqualTo(newStatus);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_shouldThrowExceptionForInvalidTransition() {
        // Given
        UUID orderId = order.getId();
        order.setOrderStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cannot change status from");
    }
}
