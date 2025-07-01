package com.ocommerce.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequest {
    private Integer quantity;
    private Long productId;
}
