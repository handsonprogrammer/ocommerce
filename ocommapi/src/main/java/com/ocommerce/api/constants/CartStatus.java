package com.ocommerce.api.constants;

public enum CartStatus {
    ACTIVE("A"),
    INACTIVE("I"),
    PLACED("P");

    private final String code;

    CartStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CartStatus fromCode(String code) {
        for (CartStatus status : CartStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CartStatus code: " + code);
    }
}