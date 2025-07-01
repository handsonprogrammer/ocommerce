package com.ocommerce.api.exception;

public class ProductNotFoundException extends Exception {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductNotFoundException(Throwable cause) {
        super(cause);
    }

    public ProductNotFoundException() {
        super("Product not found");
    }

    public ProductNotFoundException(Long productId) {
        super("Product with ID " + productId + " not found");
    }
}
