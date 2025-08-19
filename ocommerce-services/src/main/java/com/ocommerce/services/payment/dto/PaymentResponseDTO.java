package com.ocommerce.services.payment.dto;

import com.ocommerce.services.payment.domain.PaymentMethod;
import com.ocommerce.services.payment.domain.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PaymentResponseDTO {
    private UUID id;
    private UUID orderId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private String transactionId;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;
}
