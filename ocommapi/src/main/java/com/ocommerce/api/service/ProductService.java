package com.ocommerce.api.service;

import com.ocommerce.api.jpa.entities.Product;
import com.ocommerce.api.jpa.repositories.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling product actions.
 */
@Service
public class ProductService {

    /** The Product DAO. */
    private ProductRepository productRepository;

    /**
     * Constructor for spring injection.
     * @param productRepository
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Gets the all products available.
     * @return The list of products.
     */
    public List<Product> getProducts() {
        return productRepository.findAll(Sort.by("name"));
    }

}