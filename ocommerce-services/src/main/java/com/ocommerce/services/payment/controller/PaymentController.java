package com.ocommerce.services.payment.controller;

import com.ocommerce.services.payment.dto.InitiatePaymentRequest;
import com.ocommerce.services.payment.dto.PaymentResponse;
import com.ocommerce.services.payment.mapper.PaymentMapper;
import com.ocommerce.services.payment.service.PaymentService;
import com.ocommerce.services.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Payment Management", description = "APIs for managing payment transactions, processing payments, and handling refunds")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @PostMapping
    @Operation(
        summary = "Initiate a new payment",
        description = "Creates a new payment transaction for the specified order. Supports various payment methods including credit cards, digital wallets, and bank transfers. The request must include an idempotency key to prevent duplicate payments.",
        operationId = "initiatePayment"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Payment successfully initiated"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid payment request - validation errors or business rule violations"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid authentication required",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found or not accessible by the authenticated user",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "ORDER_NOT_FOUND",
                        "message": "Order with specified ID not found or not accessible"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Payment conflict - order already has a completed payment"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Unprocessable entity - payment processor error",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(
                description = "Payment initiation request containing order details and payment method",
                required = true,
                schema = @Schema(implementation = InitiatePaymentRequest.class)
            )
            @Valid @RequestBody InitiatePaymentRequest requestDTO) {
        var payment = paymentService.initiatePayment(
            requestDTO.getOrderId(),
            requestDTO.getPaymentMethod(),
            requestDTO.getAmount(),
            requestDTO.getIdempotencyKey()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(paymentMapper.toPaymentResponse(payment));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Retrieve payment details",
        description = "Fetches detailed information about a specific payment transaction. Only payments associated with orders owned by the authenticated user can be accessed.",
        operationId = "getPaymentById"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment details retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid authentication required",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found or not accessible by the authenticated user",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "PAYMENT_NOT_FOUND",
                        "message": "Payment with specified ID not found or not accessible"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<PaymentResponse> getPayment(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(
                description = "Unique identifier of the payment transaction",
                required = true,
                example = "550e8400-e29b-41d4-a716-446655440000",
                schema = @Schema(type = "string", format = "uuid")
            )
            @PathVariable UUID id) {
        return paymentService.getPaymentById(id)
            .map(paymentMapper::toPaymentResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/refund")
    @Operation(
        summary = "Refund a payment",
        description = "Processes a full refund for a completed payment transaction. The payment must be in COMPLETED status and belong to an order owned by the authenticated user. Partial refunds are not supported through this endpoint.",
        operationId = "refundPayment"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment refunded successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid refund request - payment cannot be refunded"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid authentication required",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found or not accessible by the authenticated user",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "PAYMENT_NOT_FOUND",
                        "message": "Payment with specified ID not found or not accessible"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Unprocessable entity - refund processor error"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<PaymentResponse> refundPayment(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(
                description = "Unique identifier of the payment transaction to refund",
                required = true,
                example = "550e8400-e29b-41d4-a716-446655440000",
                schema = @Schema(type = "string", format = "uuid")
            )
            @PathVariable UUID id) {
        var refundedPayment = paymentService.refundPayment(id);
        return ResponseEntity.ok(paymentMapper.toPaymentResponse(refundedPayment));
    }
}
