package com.ocommerce.services.payment.dto;

import com.ocommerce.services.payment.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Initiate payment request")
@Data
public class InitiatePaymentRequest {
    @Schema(description = "Order ID for which payment is initiated", example = "550e8400-e29b-41d4-a716-446655440200")
    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @Schema(description = "Payment method to use", example = "CREDIT_CARD")
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Schema(description = "Amount to be paid", example = "299.99")
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Schema(description = "Optional idempotency key for payment request", example = "payment-req-123456")
    private String idempotencyKey;
}

