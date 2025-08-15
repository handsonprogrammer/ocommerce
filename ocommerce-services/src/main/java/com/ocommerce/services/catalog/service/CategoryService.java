package com.ocommerce.services.catalog.service;

import com.ocommerce.services.catalog.domain.Category;
import com.ocommerce.services.catalog.dto.CategoryResponse;
import com.ocommerce.services.catalog.repository.CategoryRepository;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for Category domain operations
 */
@Service
@Transactional
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Get all root categories (categories without parent)
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        logger.info("Fetching all root categories");
        List<Category> rootCategories = categoryRepository.findByParentIdIsNullAndIsActiveTrue();
        return rootCategories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public Optional<CategoryResponse> getCategoryById(UUID categoryId) {
        logger.info("Fetching category with ID: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .filter(Category::isActive)
                .map(this::convertToCategoryResponse);
    }

    /**
     * Get category by slug
     */
    @Transactional(readOnly = true)
    public Optional<CategoryResponse> getCategoryBySlug(String slug) {
        logger.info("Fetching category with slug: {}", slug);
        return categoryRepository.findBySlugAndActive(slug)
                .map(this::convertToCategoryResponse);
    }

    /**
     * Get child categories of a parent category
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildCategories(UUID parentId) {
        logger.info("Fetching child categories for parent ID: {}", parentId);
        List<Category> childCategories = categoryRepository.findByParentIdAndIsActiveTrue(parentId);
        return childCategories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all categories ordered by sort order
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        logger.info("Fetching all active categories");
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get categories by level in hierarchy
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByLevel(Integer level) {
        logger.info("Fetching categories at level: {}", level);
        List<Category> categories = categoryRepository.findByLevelAndIsActiveTrue(level);
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search categories by name
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategoriesByName(String searchTerm) {
        logger.info("Searching categories with term: {}", searchTerm);
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCaseAndActive(searchTerm);
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Text search across categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> textSearchCategories(String searchText) {
        logger.info("Text search in categories with: {}", searchText);
        List<Category> categories = categoryRepository.findByTextSearch(searchText);
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get category hierarchy tree starting from root
     */
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        logger.info("Building category tree");
        List<Category> rootCategories = categoryRepository.findByParentIdIsNullAndIsActiveTrue();
        return rootCategories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }

    /**
     * Get categories with product count greater than specified value
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesWithMinProducts(Long minProductCount) {
        logger.info("Fetching categories with at least {} products", minProductCount);
        List<Category> categories = categoryRepository.findByProductCountGreaterThanAndIsActiveTrue(minProductCount);
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get total count of active categories
     */
    @Transactional(readOnly = true)
    public long getActiveCategoriesCount() {
        return categoryRepository.countByIsActiveTrue();
    }

    // Private helper methods

    /**
     * Convert Category entity to CategoryResponse DTO
     */
    private CategoryResponse convertToCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setThumbnailUrl(category.getThumbnailUrl());
        if (category.getParent() != null) {
            response.setParentId(category.getParent().getId());
        } else {
            response.setParentId(null);
        }
        if (category.getChildren() != null) {
            response.setChildIds(category.getChildren().stream()
                    .map(Category::getId)
                    .collect(Collectors.toList()));
        } else {
            response.setChildIds(null);
        }
        response.setSortOrder(category.getSortOrder());
        response.setLevel(category.getLevel());
        response.setPath(category.getPath());
        response.setActive(category.isActive());
        response.setProductCount(category.getProductCount());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        // Convert SEO metadata if present
        if (category.getSeoMetadata() != null) {
            CategoryResponse.SeoMetadataResponse seoResponse = new CategoryResponse.SeoMetadataResponse();
            seoResponse.setMetaTitle(category.getSeoMetadata().getMetaTitle());
            seoResponse.setMetaDescription(category.getSeoMetadata().getMetaDescription());
            seoResponse.setMetaKeywords(category.getSeoMetadata().getMetaKeywords());
            seoResponse.setSlug(category.getSeoMetadata().getSlug());
            seoResponse.setCanonicalUrl(category.getSeoMetadata().getCanonicalUrl());
            response.setSeoMetadata(seoResponse);
        }

        return response;
    }

    /**
     * Build category tree with children recursively
     */
    private CategoryTreeResponse buildCategoryTree(Category category) {
        CategoryTreeResponse treeResponse = new CategoryTreeResponse();
        treeResponse.setId(category.getId());
        treeResponse.setName(category.getName());
        treeResponse.setDescription(category.getDescription());
        treeResponse.setThumbnailUrl(category.getThumbnailUrl());
        treeResponse.setLevel(category.getLevel());
        treeResponse.setPath(category.getPath());
        treeResponse.setProductCount(category.getProductCount());
        treeResponse.setSortOrder(category.getSortOrder());

        // Add SEO slug if available
        if (category.getSeoMetadata() != null && category.getSeoMetadata().getSlug() != null) {
            treeResponse.setSlug(category.getSeoMetadata().getSlug());
        }

        // Recursively build children
        if (category.hasChildren()) {
            List<Category> children = categoryRepository.findByParentIdAndIsActiveTrue(category.getId());
            List<CategoryTreeResponse> childrenResponse = children.stream()
                    .map(this::buildCategoryTree)
                    .collect(Collectors.toList());
            treeResponse.setChildren(childrenResponse);
        }

        return treeResponse;
    }

    /**
     * Category tree response DTO for hierarchical view
     */
    public static class CategoryTreeResponse {
        private UUID id;
        private String name;
        private String description;
        private String thumbnailUrl;
        private Integer level;
        private String path;
        private String slug;
        private Long productCount;
        private Integer sortOrder;
        private List<CategoryTreeResponse> children;

        // Constructors, getters, and setters
        public CategoryTreeResponse() {}

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public Long getProductCount() {
            return productCount;
        }

        public void setProductCount(Long productCount) {
            this.productCount = productCount;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        public List<CategoryTreeResponse> getChildren() {
            return children;
        }

        public void setChildren(List<CategoryTreeResponse> children) {
            this.children = children;
        }
    }
}
