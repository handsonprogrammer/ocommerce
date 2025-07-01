package com.ocommerce.api.exception;

public class OrderLockedException extends RuntimeException {
    public OrderLockedException(String message) {
        super(message);
    }

    public OrderLockedException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderLockedException(Throwable cause) {
        super(cause);
    }

    public OrderLockedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
