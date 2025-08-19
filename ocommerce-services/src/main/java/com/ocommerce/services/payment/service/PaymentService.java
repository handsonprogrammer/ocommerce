package com.ocommerce.services.payment.service;

import com.ocommerce.services.cart.dto.ProductPricingInfo;
import com.ocommerce.services.cart.exception.ProductValidationException;
import com.ocommerce.services.cart.service.ProductValidationService;
import com.ocommerce.services.payment.domain.Payment;
import com.ocommerce.services.payment.domain.PaymentMethod;
import com.ocommerce.services.payment.domain.PaymentStatus;
import com.ocommerce.services.payment.repository.PaymentRepository;
import com.ocommerce.services.order.repository.OrderRepository;
import com.ocommerce.services.order.domain.Order;
import com.ocommerce.services.order.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final ProductValidationService productValidationService;

    @Transactional
    public Payment initiatePayment(UUID orderId, PaymentMethod paymentMethod, BigDecimal amount, String idempotencyKey) {
        log.info("Initiating payment for order: {} with method: {} and amount: {}", orderId, paymentMethod, amount);

        // Check for idempotency if key provided
        if (idempotencyKey != null) {
            Optional<Payment> existingPayment = paymentRepository.findByTransactionId(idempotencyKey);
            if (existingPayment.isPresent()) {
                log.info("Returning existing payment for idempotency key: {}", idempotencyKey);
                return existingPayment.get();
            }
        }

        // Validate order exists
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Re-validate order total with current product prices before payment
        BigDecimal calculatedTotal = validateAndCalculateOrderTotal(order);

        // Validate requested payment amount matches calculated total
        if (amount.compareTo(calculatedTotal) != 0) {
            throw new RuntimeException(
                String.format("Payment amount (%.2f) does not match calculated order total (%.2f). " +
                             "Product prices may have changed since order creation.",
                             amount.doubleValue(), calculatedTotal.doubleValue()));
        }

        // Update order total if it has changed due to price updates
        if (order.getTotalAmount().compareTo(calculatedTotal) != 0) {
            log.warn("Order total updated from {} to {} due to price changes",
                    order.getTotalAmount(), calculatedTotal);
            order.setTotalAmount(calculatedTotal);
            orderRepository.save(order);
        }

        // Check if payment already exists for this order
        Optional<Payment> existingOrderPayment = paymentRepository.findByOrderIdAndPaymentStatus(
            orderId, PaymentStatus.COMPLETED);
        if (existingOrderPayment.isPresent()) {
            throw new RuntimeException("Payment already completed for order: " + orderId);
        }

        // Create new payment with validated amount
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(calculatedTotal); // Use calculated total
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionId(idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString());

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        // Process payment through gateway
        try {
            String gatewayResponse = paymentGatewayService.processPayment(payment);
            savedPayment.setGatewayResponse(gatewayResponse);
            savedPayment.setPaymentStatus(PaymentStatus.COMPLETED);

            // Update order payment status
            order.setPaymentStatus(com.ocommerce.services.order.domain.PaymentStatus.COMPLETED);
            orderRepository.save(order);

            log.info("Payment completed successfully for order: {} with amount: {}", orderId, calculatedTotal);
        } catch (Exception e) {
            log.error("Payment failed for order: {}", orderId, e);
            savedPayment.setPaymentStatus(PaymentStatus.FAILED);
            savedPayment.setFailureReason(e.getMessage());
        }

        return paymentRepository.save(savedPayment);
    }

    /**
     * Validate all order items and calculate current total with latest product prices
     */
    private BigDecimal validateAndCalculateOrderTotal(Order order) {
        log.info("Validating and calculating order total for order: {}", order.getId());

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("Cannot process payment for order with no items");
        }

        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (OrderItem item : order.getItems()) {
            try {
                // Validate product and get current pricing
                ProductPricingInfo currentPricing = productValidationService.validateAndGetProductPricing(
                    item.getProductId(), item.getVariantId());

                // Validate stock availability
                if (!productValidationService.validateStockAvailability(
                    item.getProductId(), item.getVariantId(), item.getQuantity())) {
                    throw new ProductValidationException(
                        "Insufficient stock for product: " + currentPricing.getProductName());
                }

                // Calculate item total with current pricing
                BigDecimal itemTotal = currentPricing.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                calculatedTotal = calculatedTotal.add(itemTotal);

                log.debug("Validated item: {} - Current price: {}, Quantity: {}, Total: {}",
                    currentPricing.getProductName(), currentPricing.getPrice(), item.getQuantity(), itemTotal);

            } catch (ProductValidationException e) {
                log.error("Product validation failed during payment processing: {}", e.getMessage());
                throw new RuntimeException("Payment validation failed: " + e.getMessage(), e);
            }
        }

        log.info("Calculated order total: {} for order: {}", calculatedTotal, order.getId());
        return calculatedTotal;
    }

    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Transactional
    public Payment refundPayment(UUID paymentId) {
        log.info("Initiating refund for payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new RuntimeException("Payment is already refunded");
        }

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Cannot refund payment that is not completed");
        }

        try {
            // Process refund through gateway
            String refundResponse = paymentGatewayService.processRefund(payment);
            payment.setGatewayResponse(refundResponse);
            payment.setPaymentStatus(PaymentStatus.REFUNDED);

            // Update order payment status
            Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found for payment: " + paymentId));
            order.setPaymentStatus(com.ocommerce.services.order.domain.PaymentStatus.REFUNDED);
            orderRepository.save(order);

            log.info("Payment refunded successfully: {}", paymentId);
        } catch (Exception e) {
            log.error("Refund failed for payment: {}", paymentId, e);
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).stream().findFirst();
    }
}
