package com.ocommerce.services.catalog.repository;

import com.ocommerce.services.catalog.domain.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MongoDB repository for Category domain
 */
@Repository
public interface CategoryRepository extends MongoRepository<Category, UUID> {

    /**
     * Find all root categories (categories without parent)
     */
    List<Category> findByParentIdIsNullAndIsActiveTrue();

    /**
     * Find all child categories of a parent
     */
    List<Category> findByParentIdAndIsActiveTrue(UUID parentId);

    /**
     * Find category by slug for SEO URLs
     */
    @Query("{'seo_metadata.slug': ?0, 'is_active': true}")
    Optional<Category> findBySlugAndActive(String slug);

    /**
     * Find categories by level in hierarchy
     */
    List<Category> findByLevelAndIsActiveTrue(Integer level);

    /**
     * Find categories by name (case-insensitive search)
     */
    @Query("{'name': {$regex: ?0, $options: 'i'}, 'is_active': true}")
    List<Category> findByNameContainingIgnoreCaseAndActive(String name);

    /**
     * Find all active categories
     */
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * Find categories by path pattern (for breadcrumb navigation)
     */
    @Query("{'path': {$regex: ?0}, 'is_active': true}")
    List<Category> findByPathContainingAndActive(String pathPattern);

    /**
     * Count active categories
     */
    long countByIsActiveTrue();

    /**
     * Find categories with product count greater than specified value
     */
    List<Category> findByProductCountGreaterThanAndIsActiveTrue(Long minProductCount);

    /**
     * Text search across name and description
     */
    @Query("{ $text: { $search: ?0 }, 'is_active': true }")
    List<Category> findByTextSearch(String searchText);
}
