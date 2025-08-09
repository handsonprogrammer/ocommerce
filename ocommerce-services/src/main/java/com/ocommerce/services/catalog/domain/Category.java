package com.ocommerce.services.catalog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Category document for MongoDB catalog domain
 * Supports hierarchical category structure
 */
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

    @Field("parent_id")
    @Indexed
    private UUID parentId;

    @Field("child_ids")
    private List<UUID> childIds;

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

    // Constructors
    public Category() {
        this.id = UUID.randomUUID();
        this.isActive = true;
        this.productCount = 0L;
        this.level = 0;
    }

    public Category(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
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

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public List<UUID> getChildIds() {
        return childIds;
    }

    public void setChildIds(List<UUID> childIds) {
        this.childIds = childIds;
    }

    public SeoMetadata getSeoMetadata() {
        return seoMetadata;
    }

    public void setSeoMetadata(SeoMetadata seoMetadata) {
        this.seoMetadata = seoMetadata;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Long getProductCount() {
        return productCount;
    }

    public void setProductCount(Long productCount) {
        this.productCount = productCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Helper methods
    public boolean isRootCategory() {
        return parentId == null;
    }

    public boolean hasChildren() {
        return childIds != null && !childIds.isEmpty();
    }

    /**
     * Embedded SEO metadata for categories
     */
    public static class SeoMetadata {
        @Field("meta_title")
        private String metaTitle;

        @Field("meta_description")
        private String metaDescription;

        @Field("meta_keywords")
        private List<String> metaKeywords;

        @Field("slug")
        @Indexed(unique = true)
        private String slug;

        @Field("canonical_url")
        private String canonicalUrl;

        // Constructors, getters, and setters
        public SeoMetadata() {}

        public String getMetaTitle() {
            return metaTitle;
        }

        public void setMetaTitle(String metaTitle) {
            this.metaTitle = metaTitle;
        }

        public String getMetaDescription() {
            return metaDescription;
        }

        public void setMetaDescription(String metaDescription) {
            this.metaDescription = metaDescription;
        }

        public List<String> getMetaKeywords() {
            return metaKeywords;
        }

        public void setMetaKeywords(List<String> metaKeywords) {
            this.metaKeywords = metaKeywords;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getCanonicalUrl() {
            return canonicalUrl;
        }

        public void setCanonicalUrl(String canonicalUrl) {
            this.canonicalUrl = canonicalUrl;
        }
    }
}
