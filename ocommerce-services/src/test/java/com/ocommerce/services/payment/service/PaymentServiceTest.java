package com.ocommerce.services.payment.service;

import com.ocommerce.services.cart.dto.ProductPricingInfo;
import com.ocommerce.services.cart.service.ProductValidationService;
import com.ocommerce.services.order.domain.Order;
import com.ocommerce.services.order.domain.OrderItem;
import com.ocommerce.services.order.repository.OrderRepository;
import com.ocommerce.services.payment.domain.Payment;
import com.ocommerce.services.payment.domain.PaymentMethod;
import com.ocommerce.services.payment.domain.PaymentStatus;
import com.ocommerce.services.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @Mock
    private ProductValidationService productValidationService;

    @InjectMocks
    private PaymentService paymentService;

    private UUID orderId;
    private UUID productId;
    private UUID variantId;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String idempotencyKey;
    private Order order;
    private Payment payment;
    private OrderItem orderItem;
    private ProductPricingInfo pricingInfo;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        productId = UUID.randomUUID();
        variantId = UUID.randomUUID();
        paymentMethod = PaymentMethod.CREDIT_CARD;
        amount = BigDecimal.valueOf(200.00);
        idempotencyKey = "test-key-123";

        // Setup OrderItem first
        orderItem = new OrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setProductId(productId);
        orderItem.setVariantId(variantId);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(100.00));
        orderItem.setTotalPrice(amount);

        // Setup Order with items
        order = new Order();
        order.setId(orderId);
        order.setTotalAmount(amount);
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);
        order.setItems(items);
        orderItem.setOrder(order); // Set the back reference

        // Setup Payment
        payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionId(idempotencyKey);

        // Setup ProductPricingInfo
        pricingInfo = ProductPricingInfo.builder()
            .productId(productId)
            .variantId(variantId)
            .productName("Test Product")
            .price(BigDecimal.valueOf(100.00))
            .isActive(true)
            .inventoryTracking(true)
            .availableStock(10)
            .build();
    }

    @Test
    void initiatePayment_shouldCreatePaymentSuccessfully() {
        // Given
        when(paymentRepository.findByTransactionId(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.COMPLETED)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentGatewayService.processPayment(any(Payment.class))).thenReturn("Success");
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Mock product validation
        when(productValidationService.validateAndGetProductPricing(eq(productId), eq(variantId)))
            .thenReturn(pricingInfo);
        when(productValidationService.validateStockAvailability(eq(productId), eq(variantId), eq(2)))
            .thenReturn(true);

        // When
        Payment result = paymentService.initiatePayment(orderId, paymentMethod, amount, idempotencyKey);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(result.getAmount()).isEqualTo(amount);
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentGatewayService).processPayment(any(Payment.class));
        verify(productValidationService).validateAndGetProductPricing(productId, variantId);
        verify(productValidationService).validateStockAvailability(productId, variantId, 2);
    }

    @Test
    void initiatePayment_shouldReturnExistingPaymentForIdempotencyKey() {
        // Given
        when(paymentRepository.findByTransactionId(idempotencyKey)).thenReturn(Optional.of(payment));

        // When
        Payment result = paymentService.initiatePayment(orderId, paymentMethod, amount, idempotencyKey);

        // Then
        assertThat(result).isEqualTo(payment);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentGatewayService, never()).processPayment(any(Payment.class));
        verify(productValidationService, never()).validateAndGetProductPricing(any(), any());
    }

    @Test
    void initiatePayment_shouldThrowExceptionIfOrderNotFound() {
        // Given
        when(paymentRepository.findByTransactionId(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.initiatePayment(orderId, paymentMethod, amount, idempotencyKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    void initiatePayment_shouldThrowExceptionIfAmountMismatch() {
        // Given
        BigDecimal wrongAmount = BigDecimal.valueOf(300.00);
        when(paymentRepository.findByTransactionId(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mock product validation to return current pricing
        when(productValidationService.validateAndGetProductPricing(eq(productId), eq(variantId)))
            .thenReturn(pricingInfo);
        when(productValidationService.validateStockAvailability(eq(productId), eq(variantId), eq(2)))
            .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> paymentService.initiatePayment(orderId, paymentMethod, wrongAmount, idempotencyKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Payment amount")
            .hasMessageContaining("does not match calculated order total");
    }

    @Test
    void initiatePayment_shouldThrowExceptionIfPaymentAlreadyCompleted() {
        // Given
        Payment completedPayment = new Payment();
        completedPayment.setPaymentStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByTransactionId(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.COMPLETED)).thenReturn(Optional.of(completedPayment));

        // Mock product validation
        when(productValidationService.validateAndGetProductPricing(eq(productId), eq(variantId)))
            .thenReturn(pricingInfo);
        when(productValidationService.validateStockAvailability(eq(productId), eq(variantId), eq(2)))
            .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> paymentService.initiatePayment(orderId, paymentMethod, amount, idempotencyKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Payment already completed for order");
    }

    @Test
    void initiatePayment_shouldHandleGatewayFailure() {
        // Given
        when(paymentRepository.findByTransactionId(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.COMPLETED)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentGatewayService.processPayment(any(Payment.class))).thenThrow(new RuntimeException("Gateway error"));

        // Mock product validation
        when(productValidationService.validateAndGetProductPricing(eq(productId), eq(variantId)))
            .thenReturn(pricingInfo);
        when(productValidationService.validateStockAvailability(eq(productId), eq(variantId), eq(2)))
            .thenReturn(true);

        // When
        Payment result = paymentService.initiatePayment(orderId, paymentMethod, amount, idempotencyKey);

        // Then
        assertThat(result).isNotNull(); // Verify result is returned even on gateway failure
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentGatewayService).processPayment(any(Payment.class));
        verify(productValidationService).validateAndGetProductPricing(productId, variantId);
    }

    @Test
    void initiatePayment_shouldThrowExceptionIfProductValidationFails() {
        // Given
        when(paymentRepository.findByTransactionId(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mock product validation failure
        when(productValidationService.validateAndGetProductPricing(eq(productId), eq(variantId)))
            .thenThrow(new RuntimeException("Product not found"));

        // When & Then
        assertThatThrownBy(() -> paymentService.initiatePayment(orderId, paymentMethod, amount, idempotencyKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Product not found");
    }

    @Test
    void initiatePayment_shouldThrowExceptionIfInsufficientStock() {
        // Given
        when(paymentRepository.findByTransactionId(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mock product validation with insufficient stock
        when(productValidationService.validateAndGetProductPricing(eq(productId), eq(variantId)))
            .thenReturn(pricingInfo);
        when(productValidationService.validateStockAvailability(eq(productId), eq(variantId), eq(2)))
            .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> paymentService.initiatePayment(orderId, paymentMethod, amount, idempotencyKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Payment validation failed")
            .hasMessageContaining("Insufficient stock");
    }

    @Test
    void refundPayment_shouldRefundSuccessfully() {
        // Given
        UUID paymentId = payment.getId();
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentGatewayService.processRefund(payment)).thenReturn("Refund successful");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.refundPayment(paymentId);

        // Then
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentGatewayService).processRefund(payment);
        verify(paymentRepository).save(payment);
    }

    @Test
    void refundPayment_shouldThrowExceptionIfPaymentNotFound() {
        // Given
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.refundPayment(paymentId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Payment not found");
    }

    @Test
    void refundPayment_shouldThrowExceptionIfPaymentNotCompleted() {
        // Given
        UUID paymentId = payment.getId();
        payment.setPaymentStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> paymentService.refundPayment(paymentId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cannot refund payment that is not completed");
    }

    @Test
    void refundPayment_shouldThrowExceptionIfAlreadyRefunded() {
        // Given
        UUID paymentId = payment.getId();
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> paymentService.refundPayment(paymentId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Payment is already refunded");
    }

    @Test
    void getPaymentById_shouldReturnPayment() {
        // Given
        UUID paymentId = payment.getId();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When
        Optional<Payment> result = paymentService.getPaymentById(paymentId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(payment);
    }
}
