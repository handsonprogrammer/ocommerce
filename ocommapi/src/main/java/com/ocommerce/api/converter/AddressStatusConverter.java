package com.ocommerce.api.converter;

import com.ocommerce.api.constants.AddressStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AddressStatusConverter implements AttributeConverter<AddressStatus, String> {

    @Override
    public String convertToDatabaseColumn(AddressStatus status) {
        return status != null ? status.getCode() : null;
    }

    @Override
    public AddressStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return AddressStatus.fromCode(dbData);
    }
}