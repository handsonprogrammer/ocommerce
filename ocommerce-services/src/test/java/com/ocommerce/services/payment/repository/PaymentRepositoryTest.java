package com.ocommerce.services.payment.repository;

import com.ocommerce.services.payment.domain.Payment;
import com.ocommerce.services.payment.domain.PaymentMethod;
import com.ocommerce.services.payment.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private UUID orderId;
    private Payment payment1;
    private Payment payment2;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        // Create first payment
        payment1 = new Payment();
        payment1.setOrderId(orderId);
        payment1.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment1.setPaymentStatus(PaymentStatus.COMPLETED);
        payment1.setAmount(BigDecimal.valueOf(200.00));
        payment1.setTransactionId("TXN-123456");
        payment1.setGatewayResponse("Success");

        // Create second payment (for different order)
        payment2 = new Payment();
        payment2.setOrderId(UUID.randomUUID());
        payment2.setPaymentMethod(PaymentMethod.UPI);
        payment2.setPaymentStatus(PaymentStatus.PENDING);
        payment2.setAmount(BigDecimal.valueOf(150.00));
        payment2.setTransactionId("TXN-789012");
    }

    @Test
    void findByOrderId_shouldReturnPaymentsForOrder() {
        // Given
        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        // When
        List<Payment> result = paymentRepository.findByOrderId(orderId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(orderId);
        assertThat(result.get(0).getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
    }

    @Test
    void findByOrderIdAndPaymentStatus_shouldReturnPaymentWithSpecificStatus() {
        // Given
        paymentRepository.save(payment1);

        // When
        Optional<Payment> result = paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.COMPLETED);
        Optional<Payment> notFound = paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.FAILED);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(notFound).isEmpty();
    }

    @Test
    void findByTransactionId_shouldReturnPaymentWithTransactionId() {
        // Given
        paymentRepository.save(payment1);

        // When
        Optional<Payment> result = paymentRepository.findByTransactionId("TXN-123456");
        Optional<Payment> notFound = paymentRepository.findByTransactionId("NON-EXISTENT");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-123456");
        assertThat(notFound).isEmpty();
    }

    @Test
    void findByPaymentStatus_shouldReturnPaymentsWithStatus() {
        // Given
        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        // When
        List<Payment> completedPayments = paymentRepository.findByPaymentStatus(PaymentStatus.COMPLETED);
        List<Payment> pendingPayments = paymentRepository.findByPaymentStatus(PaymentStatus.PENDING);

        // Then
        assertThat(completedPayments).hasSize(1);
        assertThat(completedPayments.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(pendingPayments).hasSize(1);
        assertThat(pendingPayments.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void countByOrderId_shouldReturnCorrectCount() {
        // Given
        paymentRepository.save(payment1);
        // Add another payment for the same order
        Payment anotherPayment = new Payment();
        anotherPayment.setOrderId(orderId);
        anotherPayment.setPaymentMethod(PaymentMethod.PAYPAL);
        anotherPayment.setPaymentStatus(PaymentStatus.FAILED);
        anotherPayment.setAmount(BigDecimal.valueOf(200.00));
        anotherPayment.setTransactionId("TXN-FAILED");
        paymentRepository.save(anotherPayment);

        // When
        long count = paymentRepository.countByOrderId(orderId);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void save_shouldPersistPayment() {
        // When
        Payment savedPayment = paymentRepository.save(payment1);

        // Then
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getOrderId()).isEqualTo(orderId);
        assertThat(savedPayment.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(savedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(200.00));
        assertThat(savedPayment.getCreatedAt()).isNotNull();
        assertThat(savedPayment.getUpdatedAt()).isNotNull();
    }

    @Test
    void save_shouldUpdateExistingPayment() {
        // Given
        Payment savedPayment = paymentRepository.save(payment1);
        UUID originalPaymentId = savedPayment.getId();

        // Modify payment
        savedPayment.setPaymentStatus(PaymentStatus.REFUNDED);
        savedPayment.setGatewayResponse("Refunded successfully");

        // When
        Payment updatedPayment = paymentRepository.save(savedPayment);

        // Then
        assertThat(updatedPayment.getId()).isEqualTo(originalPaymentId);
        assertThat(updatedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(updatedPayment.getGatewayResponse()).isEqualTo("Refunded successfully");
    }

    @Test
    void findById_shouldReturnPaymentWhenExists() {
        // Given
        Payment savedPayment = paymentRepository.save(payment1);

        // When
        Optional<Payment> result = paymentRepository.findById(savedPayment.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedPayment.getId());
    }

    @Test
    void delete_shouldRemovePayment() {
        // Given
        Payment savedPayment = paymentRepository.save(payment1);
        UUID paymentId = savedPayment.getId();

        // When
        paymentRepository.delete(savedPayment);

        // Then
        Optional<Payment> result = paymentRepository.findById(paymentId);
        assertThat(result).isEmpty();
    }
}
