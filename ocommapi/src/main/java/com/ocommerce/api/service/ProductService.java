package com.ocommerce.api.service;

import com.ocommerce.api.exception.ProductNotFoundException;
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
     * 
     * @param productRepository
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Gets the all products available.
     * 
     * @return The list of products.
     */
    public List<Product> getProducts() {
        return productRepository.findAll(Sort.by("name"));
    }

    public Product getProductById(Long productId) throws ProductNotFoundException {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
    }

    /**
     * Saves a product.
     * 
     * @param product The product to save.
     * @return The saved product.
     *
     *         public Product saveProduct(Product product) {
     *         return productRepository.save(product);
     *         }
     * 
     *         /**
     *         Deletes a product by its ID.
     * 
     * @param productId The ID of the product to delete.
     *
     *                  public void deleteProduct(Long productId) {
     *                  productRepository.deleteById(productId);
     *                  }
     * 
     *                  /**
     *                  Updates a product.
     * 
     * @param product   The product to update.
     * @return The updated product.
     * @throws ProductNotFoundException
     *
     *                                  public Product updateProduct(Product
     *                                  product) throws ProductNotFoundException {
     *                                  if
     *                                  (!productRepository.existsById(product.getProductId()))
     *                                  {
     *                                  throw new ProductNotFoundException("Product
     *                                  not found with id: " +
     *                                  product.getProductId());
     *                                  }
     *                                  return productRepository.save(product);
     *                                  }
     */
}