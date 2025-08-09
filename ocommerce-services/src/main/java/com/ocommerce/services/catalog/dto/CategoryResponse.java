package com.ocommerce.services.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Category response DTO for API responses
 */
@Schema(description = "Category information")
public class CategoryResponse {

    @Schema(description = "Category unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("id")
    private UUID id;

    @Schema(description = "Category name", example = "Electronics")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Category description", example = "Electronic devices and accessories")
    @JsonProperty("description")
    private String description;

    @Schema(description = "Category thumbnail image URL", example = "https://example.com/images/electronics-thumb.jpg")
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;

    @Schema(description = "Parent category ID", example = "123e4567-e89b-12d3-a456-426614174001")
    @JsonProperty("parentId")
    private UUID parentId;

    @Schema(description = "List of child category IDs")
    @JsonProperty("childIds")
    private List<UUID> childIds;

    @Schema(description = "SEO metadata")
    @JsonProperty("seoMetadata")
    private SeoMetadataResponse seoMetadata;

    @Schema(description = "Sort order for display", example = "1")
    @JsonProperty("sortOrder")
    private Integer sortOrder;

    @Schema(description = "Category level in hierarchy", example = "0")
    @JsonProperty("level")
    private Integer level;

    @Schema(description = "Category path", example = "/electronics/computers")
    @JsonProperty("path")
    private String path;

    @Schema(description = "Whether category is active", example = "true")
    @JsonProperty("isActive")
    private boolean isActive;

    @Schema(description = "Number of products in this category", example = "25")
    @JsonProperty("productCount")
    private Long productCount;

    @Schema(description = "Category creation timestamp")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "Category last update timestamp")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    // Constructors
    public CategoryResponse() {}

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

    public SeoMetadataResponse getSeoMetadata() {
        return seoMetadata;
    }

    public void setSeoMetadata(SeoMetadataResponse seoMetadata) {
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

    /**
     * SEO metadata response DTO
     */
    public static class SeoMetadataResponse {
        @Schema(description = "SEO meta title", example = "Electronics - Best Deals Online")
        @JsonProperty("metaTitle")
        private String metaTitle;

        @Schema(description = "SEO meta description", example = "Shop the latest electronics with great deals")
        @JsonProperty("metaDescription")
        private String metaDescription;

        @Schema(description = "SEO meta keywords")
        @JsonProperty("metaKeywords")
        private List<String> metaKeywords;

        @Schema(description = "URL slug", example = "electronics")
        @JsonProperty("slug")
        private String slug;

        @Schema(description = "Canonical URL", example = "https://example.com/categories/electronics")
        @JsonProperty("canonicalUrl")
        private String canonicalUrl;

        // Constructors, getters, and setters
        public SeoMetadataResponse() {}

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
