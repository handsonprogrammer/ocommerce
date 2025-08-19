package com.ocommerce.services.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocommerce.services.config.WithCustomUser;
import com.ocommerce.services.payment.domain.Payment;
import com.ocommerce.services.payment.domain.PaymentMethod;
import com.ocommerce.services.payment.domain.PaymentStatus;
import com.ocommerce.services.payment.dto.InitiatePaymentRequestDTO;
import com.ocommerce.services.payment.dto.PaymentResponseDTO;
import com.ocommerce.services.payment.mapper.PaymentMapper;
import com.ocommerce.services.payment.service.PaymentService;
import com.ocommerce.services.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.ocommerce.services.user.UserConstants.*;
import static com.ocommerce.services.user.UserConstants.ACCOUNT_ENABLED;
import static com.ocommerce.services.user.UserConstants.EMAIL_VERIFIED;
import static com.ocommerce.services.user.UserConstants.LAST_NAME;
import static com.ocommerce.services.user.UserConstants.PHONE_NUMBER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@ActiveProfiles("test")
@WithCustomUser(email = EMAIL, userId = USER_ID,
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
        phoneNumber = PHONE_NUMBER,
        accountEnabled = ACCOUNT_ENABLED,
        emailVerified = EMAIL_VERIFIED)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentMapper paymentMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    private Payment payment;
    private PaymentResponseDTO paymentResponseDTO;
    private InitiatePaymentRequestDTO initiatePaymentRequestDTO;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(UUID.randomUUID());
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setAmount(BigDecimal.valueOf(200.00));
        payment.setTransactionId("TXN-123456");
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setId(payment.getId());
        paymentResponseDTO.setOrderId(payment.getOrderId());
        paymentResponseDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        paymentResponseDTO.setPaymentStatus(PaymentStatus.COMPLETED);
        paymentResponseDTO.setAmount(BigDecimal.valueOf(200.00));
        paymentResponseDTO.setTransactionId("TXN-123456");

        initiatePaymentRequestDTO = new InitiatePaymentRequestDTO();
        initiatePaymentRequestDTO.setOrderId(UUID.randomUUID());
        initiatePaymentRequestDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        initiatePaymentRequestDTO.setAmount(BigDecimal.valueOf(200.00));
        initiatePaymentRequestDTO.setIdempotencyKey("test-key-123");
    }

    @Test
    void initiatePayment_shouldCreatePaymentSuccessfully() throws Exception {
        // Given
        when(paymentService.initiatePayment(
            eq(initiatePaymentRequestDTO.getOrderId()),
            eq(initiatePaymentRequestDTO.getPaymentMethod()),
            eq(initiatePaymentRequestDTO.getAmount()),
            eq(initiatePaymentRequestDTO.getIdempotencyKey())
        )).thenReturn(payment);
        when(paymentMapper.toPaymentResponseDTO(payment)).thenReturn(paymentResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initiatePaymentRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(paymentResponseDTO.getId().toString()))
                .andExpect(jsonPath("$.orderId").value(paymentResponseDTO.getOrderId().toString()))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(200.00));
    }

    @Test
    void initiatePayment_shouldReturn400ForInvalidRequest() throws Exception {
        // Given
        InitiatePaymentRequestDTO invalidRequest = new InitiatePaymentRequestDTO();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initiatePayment_shouldReturn400ForNegativeAmount() throws Exception {
        // Given
        initiatePaymentRequestDTO.setAmount(BigDecimal.valueOf(-100.00));

        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initiatePaymentRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPayment_shouldReturnPayment() throws Exception {
        // Given
        UUID paymentId = payment.getId();
        when(paymentService.getPaymentById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentMapper.toPaymentResponseDTO(payment)).thenReturn(paymentResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));
    }

    @Test
    void getPayment_shouldReturn404WhenPaymentNotFound() throws Exception {
        // Given
        UUID paymentId = UUID.randomUUID();
        when(paymentService.getPaymentById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/payments/{id}", paymentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void refundPayment_shouldRefundSuccessfully() throws Exception {
        // Given
        UUID paymentId = payment.getId();
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        paymentResponseDTO.setPaymentStatus(PaymentStatus.REFUNDED);

        when(paymentService.refundPayment(paymentId)).thenReturn(payment);
        when(paymentMapper.toPaymentResponseDTO(payment)).thenReturn(paymentResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/{id}/refund", paymentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.paymentStatus").value("REFUNDED"));
    }

    @Test
    void refundPayment_shouldReturn500WhenRefundFails() throws Exception {
        // Given
        UUID paymentId = UUID.randomUUID();
        when(paymentService.refundPayment(paymentId)).thenThrow(new RuntimeException("Refund failed"));

        // When & Then
        mockMvc.perform(post("/api/v1/payments/{id}/refund", paymentId)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}
