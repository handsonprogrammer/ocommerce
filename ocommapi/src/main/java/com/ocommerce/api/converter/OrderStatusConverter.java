package com.ocommerce.api.converter;

import com.ocommerce.api.constants.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatus status) {
        return status != null ? status.getCode() : null;
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return OrderStatus.fromCode(dbData);
    }
}