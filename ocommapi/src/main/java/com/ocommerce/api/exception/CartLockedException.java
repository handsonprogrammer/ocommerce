package com.ocommerce.api.exception;

public class CartLockedException extends RuntimeException {
    public CartLockedException(String message) {
        super(message);
    }

    public CartLockedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CartLockedException(Throwable cause) {
        super(cause);
    }

    public CartLockedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CartLockedException() {
        super("Cart is locked and cannot be modified.");
    }

    public CartLockedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace, String additionalInfo) {
        super(message + " Additional info: " + additionalInfo, cause, enableSuppression, writableStackTrace);
    }

    public CartLockedException(String message, String additionalInfo) {
        super(message + " Additional info: " + additionalInfo);
    }

    public CartLockedException(Throwable cause, String additionalInfo) {
        super("Cart is locked and cannot be modified. Additional info: " + additionalInfo, cause);
    }

    public CartLockedException(String message, Throwable cause, String additionalInfo) {
        super(message + " Additional info: " + additionalInfo, cause);
    }

    public CartLockedException(String message, String additionalInfo, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message + " Additional info: " + additionalInfo, null, enableSuppression, writableStackTrace);
    }

    public CartLockedException(String message, String additionalInfo, Throwable cause) {
        super(message + " Additional info: " + additionalInfo, cause);
    }
}