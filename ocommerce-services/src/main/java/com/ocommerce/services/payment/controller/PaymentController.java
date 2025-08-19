package com.ocommerce.services.payment.controller;

import com.ocommerce.services.payment.dto.InitiatePaymentRequestDTO;
import com.ocommerce.services.payment.dto.PaymentResponseDTO;
import com.ocommerce.services.payment.mapper.PaymentMapper;
import com.ocommerce.services.payment.service.PaymentService;
import com.ocommerce.services.user.domain.User;
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
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> initiatePayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody InitiatePaymentRequestDTO requestDTO) {
        var payment = paymentService.initiatePayment(
            requestDTO.getOrderId(),
            requestDTO.getPaymentMethod(),
            requestDTO.getAmount(),
            requestDTO.getIdempotencyKey()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(paymentMapper.toPaymentResponseDTO(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPayment(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        return paymentService.getPaymentById(id)
            .map(paymentMapper::toPaymentResponseDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponseDTO> refundPayment(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        var refundedPayment = paymentService.refundPayment(id);
        return ResponseEntity.ok(paymentMapper.toPaymentResponseDTO(refundedPayment));
    }
}
