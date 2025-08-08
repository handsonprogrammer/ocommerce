package com.ocommerce.services.user.exception;

public class AddressNotFoundException extends  RuntimeException {
    public AddressNotFoundException(String message) {
        super(message);
    }
}
