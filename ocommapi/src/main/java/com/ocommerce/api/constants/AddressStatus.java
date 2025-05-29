package com.ocommerce.api.constants;

public enum AddressStatus {
    ACTIVE("A"),
    TERMINATED("T");

    private final String code;

    AddressStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AddressStatus fromCode(String code) {
        for (AddressStatus status : AddressStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AddressStatus code: " + code);
    }
}
