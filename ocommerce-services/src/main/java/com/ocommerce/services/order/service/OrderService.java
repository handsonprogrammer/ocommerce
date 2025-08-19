package com.ocommerce.services.order.service;

import com.ocommerce.services.cart.domain.Cart;
import com.ocommerce.services.cart.dto.ProductPricingInfo;
import com.ocommerce.services.cart.exception.ProductValidationException;
import com.ocommerce.services.cart.repository.CartRepository;
import com.ocommerce.services.cart.service.ProductValidationService;
import com.ocommerce.services.order.domain.Order;
import com.ocommerce.services.order.domain.OrderItem;
import com.ocommerce.services.order.domain.OrderStatus;
import com.ocommerce.services.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductValidationService productValidationService;

    @Transactional
    public Order createOrderFromCart(UUID userId, UUID shippingAddressId, UUID billingAddressId) {
        log.info("Creating order from cart for user: {}", userId);

        // Get user's cart
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot create order from empty cart");
        }

        // Validate all cart items before creating order
        validateCartItemsForOrder(cart);

        // Create new order
        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddressId(shippingAddressId);
        order.setBillingAddressId(billingAddressId);
        order.setOrderStatus(OrderStatus.PENDING);

        // Convert cart items to order items with validated pricing
        List<OrderItem> orderItems = cart.getItems().stream()
            .map(cartItem -> {
                // Re-validate product and get current pricing for order
                ProductPricingInfo pricingInfo = productValidationService.validateAndGetProductPricing(
                    cartItem.getProductId(), cartItem.getVariantId());

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setVariantId(cartItem.getVariantId());
                orderItem.setQuantity(cartItem.getQuantity());

                // Use validated pricing from catalog domain
                orderItem.setUnitPrice(pricingInfo.getPrice());
                orderItem.setProductName(pricingInfo.getProductName());
                orderItem.setVariantName(pricingInfo.getVariantName());
                orderItem.setSku(pricingInfo.getSku());

                // Calculate pricing (can be extended for discounts/taxes)
                orderItem.setDiscountAmount(BigDecimal.ZERO);
                orderItem.setTaxAmount(BigDecimal.ZERO);
                orderItem.setTotalPrice(pricingInfo.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

                return orderItem;
            })
            .collect(Collectors.toList());

        order.setItems(orderItems);
        order.setTotalAmount(order.calculateTotalAmount());

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear cart after order creation
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order created successfully with ID: {} and total amount: {}",
                savedOrder.getId(), savedOrder.getTotalAmount());
        return savedOrder;
    }

    /**
     * Validate all cart items before creating order
     */
    private void validateCartItemsForOrder(Cart cart) {
        log.info("Validating cart items for order creation: cart={}", cart.getId());

        for (var cartItem : cart.getItems()) {
            try {
                // Validate product exists and is active
                ProductPricingInfo pricingInfo = productValidationService.validateAndGetProductPricing(
                    cartItem.getProductId(), cartItem.getVariantId());

                // Validate stock availability
                if (!productValidationService.validateStockAvailability(
                    cartItem.getProductId(), cartItem.getVariantId(), cartItem.getQuantity())) {
                    throw new ProductValidationException(
                        "Insufficient stock for product: " + pricingInfo.getProductName());
                }

                log.debug("Product validation passed for: {} ({})",
                    pricingInfo.getProductName(), pricingInfo.getSku());

            } catch (ProductValidationException e) {
                log.error("Product validation failed during order creation: {}", e.getMessage());
                throw new RuntimeException("Order validation failed: " + e.getMessage(), e);
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(UUID orderId, UUID userId) {
        return orderRepository.findByIdAndUserId(orderId, userId);
    }

    @Transactional(readOnly = true)
    public Page<Order> getUserOrders(UUID userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional
    public Order cancelOrder(UUID orderId, UUID userId) {
        log.info("Cancelling order: {} for user: {}", orderId, userId);

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Check if order can be cancelled
        if (order.getOrderStatus() == OrderStatus.DELIVERED ||
            order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel order in status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order cancelled successfully: {}", orderId);
        return cancelledOrder;
    }

    @Transactional
    public Order updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        log.info("Updating order status: {} to {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Validate status transition (basic validation)
        validateStatusTransition(order.getOrderStatus(), newStatus);

        order.setOrderStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully: {} -> {}", orderId, newStatus);
        return updatedOrder;
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Basic status transition validation
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot change status from " + currentStatus);
        }

        // Add more specific validation rules as needed
        if (currentStatus == OrderStatus.SHIPPED && newStatus == OrderStatus.CONFIRMED) {
            throw new RuntimeException("Cannot go back from SHIPPED to CONFIRMED");
        }
    }
}

