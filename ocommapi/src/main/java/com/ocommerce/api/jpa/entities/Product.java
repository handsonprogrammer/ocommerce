package com.ocommerce.api.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

/**
 * A product available for purchasing.
 */
@Entity
@Data
@Table(name = "product")
public class Product {

    /** Unique id for the product. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Long productId;
    /** The name of the product. */
    @Column(name = "sku", nullable = false, unique = true)
    private String sku;
    /** The name of the product. */
    @Column(name = "name", nullable = false)
    private String name;
    /** The short description of the product. */
    @Column(name = "short_description", nullable = false)
    private String shortDescription;
    /** The long description of the product. */
    @Column(name = "long_description")
    private String longDescription;
    /** The price of the product. */
    @Column(name = "price", nullable = false, scale = 2,precision = 9)
    private BigDecimal price;
    /** The inventory of the product. */
    @OneToOne(mappedBy = "product", cascade = CascadeType.REMOVE, optional = false, orphanRemoval = true)
    private Inventory inventory;
}
