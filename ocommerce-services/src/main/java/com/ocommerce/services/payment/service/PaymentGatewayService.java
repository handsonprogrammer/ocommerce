package com.ocommerce.services.payment.service;

import com.ocommerce.services.payment.domain.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Mock Payment Gateway Service for demonstration purposes.
 * In production, this would integrate with actual payment processors like Stripe, PayPal, etc.
 */
@Service
@Slf4j
public class PaymentGatewayService {

    public String processPayment(Payment payment) {
        log.info("Processing payment through mock gateway: {}", payment.getId());

        // Simulate payment processing
        try {
            Thread.sleep(100); // Simulate gateway response time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Mock successful payment response
        String mockTransactionId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String mockResponse = String.format(
            "Mock Gateway Response: Payment processed successfully. Transaction ID: %s, Amount: %s %s",
            mockTransactionId,
            payment.getAmount(),
            payment.getPaymentMethod()
        );

        log.info("Mock payment processed: {}", mockResponse);
        return mockResponse;
    }

    public String processRefund(Payment payment) {
        log.info("Processing refund through mock gateway: {}", payment.getId());

        // Simulate refund processing
        try {
            Thread.sleep(100); // Simulate gateway response time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Mock successful refund response
        String mockRefundId = "REF_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String mockResponse = String.format(
            "Mock Gateway Response: Refund processed successfully. Refund ID: %s, Amount: %s",
            mockRefundId,
            payment.getAmount()
        );

        log.info("Mock refund processed: {}", mockResponse);
        return mockResponse;
    }
}
