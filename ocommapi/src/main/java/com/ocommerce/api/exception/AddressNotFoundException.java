package com.ocommerce.api.exception;

public class AddressNotFoundException extends Exception {

    public AddressNotFoundException(String message) {
        super(message);
    }

    public AddressNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AddressNotFoundException(Throwable cause) {
        super(cause);
    }

    public AddressNotFoundException() {
        super("Address not found");
    }

    public AddressNotFoundException(Long addressId) {
        super("Address with ID " + addressId + " not found");
    }

}
