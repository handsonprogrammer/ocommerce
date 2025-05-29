package com.ocommerce.api.constants;

public enum OrderStatus {
    PENDING("P"),
    CONFIRMED("C"),
    PROCESSING("R"),
    SHIPPED("S"),
    IN_TRANSIT("I"),
    OUT_FOR_DELIVERY("O"),
    DELIVERED("D"),
    PARTIALLY_SHIPPED("H"),
    FULFILLED("F"),
    CANCELED("X"),
    REFUNDED("U"),
    ON_HOLD("H"),
    AWAITING_PICKUP("K"),
    AWAITING_SHIPMENT("M"),
    PENDING_PAYMENT("Y");

    private final String code;

    OrderStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
    }
}