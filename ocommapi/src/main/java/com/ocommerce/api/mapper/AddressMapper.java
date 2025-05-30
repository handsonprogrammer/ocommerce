package com.ocommerce.api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.ocommerce.api.jpa.entities.Address;
import com.ocommerce.api.model.AddressDto;

public class AddressMapper {
    public static AddressDto toDto(Address address) {
        if (address == null)
            return null;
        AddressDto dto = new AddressDto();
        dto.setAddressId(address.getAddressId());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setCountry(address.getCountry());
        dto.setZipcode(address.getZipcode());
        dto.setStatus(address.getStatus() != null ? address.getStatus().getCode() : null);
        dto.setDefaultAddress(address.isDefaultAddress());
        dto.setUserId(address.getUser() != null ? address.getUser().getId() : null);
        return dto;
    }

    public static Address toEntity(AddressDto dto) {
        if (dto == null)
            return null;
        Address address = new Address();
        address.setAddressId(dto.getAddressId());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setCountry(dto.getCountry());
        address.setZipcode(dto.getZipcode());
        // Status, Default Address and user should be set in service layer as they may
        // require lookups
        return address;
    }

    public static List<AddressDto> toDtoList(List<Address> addresses) {
        return addresses.stream()
                .map(AddressMapper::toDto)
                .collect(Collectors.toList());
    }
}