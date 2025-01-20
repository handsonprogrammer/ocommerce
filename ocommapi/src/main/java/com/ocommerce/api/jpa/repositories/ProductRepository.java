package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.Product;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface ProductRepository extends ListPagingAndSortingRepository<Product, Long> {
}