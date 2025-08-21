package com.ocommerce.services.payment.repository;

import com.ocommerce.services.payment.domain.Payment;
import com.ocommerce.services.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Find payments by order ID
    List<Payment> findByOrderId(UUID orderId);

    // Find payment by order ID and status
    Optional<Payment> findByOrderIdAndPaymentStatus(UUID orderId, PaymentStatus paymentStatus);

    // Find payments by transaction ID (for idempotency)
    Optional<Payment> findByTransactionId(String transactionId);

    // Find payments by status
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    // Count payments by order ID
    long countByOrderId(UUID orderId);
}
