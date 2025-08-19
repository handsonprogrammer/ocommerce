package com.ocommerce.services.catalog.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Category document for MongoDB catalog domain
 * Supports hierarchical category structure
 */
@Data
@NoArgsConstructor
@Document(collection = "categories")
public class Category {

    @Id
    private UUID id;

    @TextIndexed(weight = 2)
    @Field("name")
    private String name;

    @TextIndexed
    @Field("description")
    private String description;

    @Field("thumbnail_url")
    private String thumbnailUrl;

    // Use MongoDB reference for parent category
    @DBRef
    @Field("parent")
    private Category parent;

    // Use MongoDB reference for child categories
    @DBRef
    @Field("children")
    private List<Category> children;

    @Field("seo_metadata")
    private SeoMetadata seoMetadata;

    @Field("sort_order")
    private Integer sortOrder;

    @Field("level")
    @Indexed
    private Integer level; // 0 for root categories, 1 for first level children, etc.

    @Field("path")
    @Indexed
    private String path; // e.g., "/electronics/computers/laptops"

    @Field("is_active")
    @Indexed
    private boolean isActive;

    @Field("product_count")
    private Long productCount; // Denormalized count for performance

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("created_by")
    private UUID createdBy;

    @Field("updated_by")
    private UUID updatedBy;

    // Custom constructors
    public Category(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.isActive = true;
        this.productCount = 0L;
        this.level = 0;
    }

    // Custom initialization for default constructor
    public void initializeDefaults() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.productCount == null) {
            this.productCount = 0L;
        }
        if (this.level == null) {
            this.level = 0;
        }
        this.isActive = true;
    }

    // Helper methods
    public boolean isRootCategory() {
        return parent == null;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * Embedded SEO metadata for categories
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeoMetadata {
        @Field("meta_title")
        private String metaTitle;

        @Field("meta_description")
        private String metaDescription;

        @Field("meta_keywords")
        private List<String> metaKeywords;

        @Field("slug")
        @Indexed
        private String slug;

        @Field("canonical_url")
        private String canonicalUrl;
    }
}
