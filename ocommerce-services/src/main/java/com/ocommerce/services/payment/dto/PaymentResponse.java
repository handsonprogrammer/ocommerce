package com.ocommerce.services.payment.dto;

import com.ocommerce.services.payment.domain.PaymentMethod;
import com.ocommerce.services.payment.domain.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Payment response")
@Data
public class PaymentResponse {
    @Schema(description = "Payment unique identifier", example = "550e8400-e29b-41d4-a716-446655440300")
    private UUID id;

    @Schema(description = "Order ID associated with the payment", example = "550e8400-e29b-41d4-a716-446655440200")
    private UUID orderId;

    @Schema(description = "Payment method used", example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "Current payment status", example = "PAID")
    private PaymentStatus paymentStatus;

    @Schema(description = "Amount paid", example = "299.99")
    private BigDecimal amount;

    @Schema(description = "Transaction ID from payment gateway", example = "txn_1234567890")
    private String transactionId;

    @Schema(description = "Reason for payment failure (if any)", example = "Insufficient funds")
    private String failureReason;

    @Schema(description = "Payment creation timestamp (UTC)", example = "2024-08-19T10:15:30Z")
    private Instant createdAt;

    @Schema(description = "Payment last update timestamp (UTC)", example = "2024-08-19T12:00:00Z")
    private Instant updatedAt;
}

