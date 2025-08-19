package com.ocommerce.services.catalog.repository;

import com.ocommerce.services.catalog.constants.ProductStatus;
import com.ocommerce.services.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MongoDB repository for Product domain
 */
@Repository
public interface ProductRepository extends MongoRepository<Product, UUID> {

    /**
     * Find all active products with pagination
     */
    Page<Product> findByStatusAndInventoryTrackingTrue(ProductStatus status, Pageable pageable);

    /**
     * Find products by category with pagination
     */
    @Query("{'category_ids': ?0, 'status': 'ACTIVE'}")
    Page<Product> findByCategoryIdAndActive(UUID categoryId, Pageable pageable);

    /**
     * Find products by multiple categories
     */
    @Query("{'category_ids': {$in: ?0}, 'status': 'ACTIVE'}")
    Page<Product> findByCategoryIdsInAndActive(List<UUID> categoryIds, Pageable pageable);

    /**
     * Find products by price range using Spring Data method naming
     */
    @Query("{'base_price': {$gte: ?0, $lte: ?1}, 'status': ?2}")
    Page<Product> findByBasePriceBetweenAndStatus(Double minPrice, Double maxPrice, ProductStatus status, Pageable pageable);

    /**
     * Find product by slug for SEO URLs
     */
    @Query("{'seo_metadata.slug': ?0, 'status': 'ACTIVE'}")
    Optional<Product> findBySlugAndActive(String slug);

    /**
     * Text search across name and descriptions
     */
    @Query("{ $text: { $search: ?0 }, 'status': 'ACTIVE' }")
    Page<Product> findByTextSearchAndActive(String searchText, Pageable pageable);

    /**
     * Find products by name containing (case-insensitive)
     */
    @Query("{'name': {$regex: ?0, $options: 'i'}, 'status': 'ACTIVE'}")
    Page<Product> findByNameContainingIgnoreCaseAndActive(String name, Pageable pageable);

    /**
     * Find products by attributes
     */
    @Query("{'attributes.?0': ?1, 'status': 'ACTIVE'}")
    Page<Product> findByAttributeAndActive(String attributeName, String attributeValue, Pageable pageable);

    /**
     * Find products with variants having specific SKU
     */
    @Query("{'variants.sku': ?0, 'status': 'ACTIVE'}")
    Optional<Product> findByVariantSkuAndActive(String sku);

    /**
     * Count products by category
     */
    @Query(value = "{'category_ids': ?0, 'status': 'ACTIVE'}", count = true)
    long countByCategoryIdAndActive(UUID categoryId);

    /**
     * Find products with low stock variants
     */
    @Query("{'variants.inventory.quantity': {$lte: 'variants.inventory.low_stock_threshold'}, 'status': 'ACTIVE'}")
    List<Product> findProductsWithLowStock();

    /**
     * Complex search with multiple filters
     */
    @Query("{ $and: [ " +
           "{'status': 'ACTIVE'}, " +
           "{$or: [ " +
           "{'name': {$regex: ?0, $options: 'i'}}, " +
           "{'short_description': {$regex: ?0, $options: 'i'}} " +
           "]}, " +
           "{'category_ids': {$in: ?1}}, " +
           "{'base_price': {$gte: ?2, $lte: ?3}} " +
           "]}")
    Page<Product> findByComplexSearch(String searchText, List<UUID> categoryIds,
                                    Double minPrice, Double maxPrice, Pageable pageable);

    /**
     * Find products by status
     */
    List<Product> findByStatus(ProductStatus status);

    /**
     * Find products created by user
     */
    Page<Product> findByCreatedBy(UUID createdBy, Pageable pageable);

    /**
     * Find products by category path containing specific hierarchy
     */
    @Query("{'category_paths': {$regex: ?0, $options: 'i'}, 'status': 'ACTIVE'}")
    Page<Product> findByCategoryPathContaining(String categoryPath, Pageable pageable);

    /**
     * Find all products by category ID (for bulk updates)
     */
    @Query("{'category_ids': ?0}")
    List<Product> findByCategoryId(UUID categoryId);

    /**
     * Find products by exact category path
     */
    @Query("{'category_paths': ?0, 'status': 'ACTIVE'}")
    Page<Product> findByExactCategoryPath(String categoryPath, Pageable pageable);

    /**
     * Find products that need category path updates (products with categories but no paths)
     */
    @Query("{'category_ids': {$exists: true, $ne: []}, 'category_paths': {$exists: false}}")
    List<Product> findProductsNeedingCategoryPaths();
}
