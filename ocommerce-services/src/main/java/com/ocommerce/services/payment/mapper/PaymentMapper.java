package com.ocommerce.services.payment.mapper;

import com.ocommerce.services.payment.domain.Payment;
import com.ocommerce.services.payment.dto.PaymentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toPaymentResponse(Payment payment);
}
