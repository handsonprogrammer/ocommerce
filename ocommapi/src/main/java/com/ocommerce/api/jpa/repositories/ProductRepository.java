package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.Product;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface ProductRepository
        extends ListCrudRepository<Product, Long>, ListPagingAndSortingRepository<Product, Long> {

    // Additional query methods can be defined here if needed
    // For example, to find products by name or category
    // List<Product> findByNameContainingIgnoreCase(String name);
    // List<Product> findByCategory_Id(Long categoryId);
}