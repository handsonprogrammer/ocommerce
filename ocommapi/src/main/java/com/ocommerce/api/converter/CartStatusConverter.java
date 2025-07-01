package com.ocommerce.api.converter;

import com.ocommerce.api.constants.CartStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CartStatusConverter implements AttributeConverter<CartStatus, String> {

    @Override
    public String convertToDatabaseColumn(CartStatus status) {
        return status != null ? status.getCode() : null;
    }

    @Override
    public CartStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return CartStatus.fromCode(dbData);
    }
}