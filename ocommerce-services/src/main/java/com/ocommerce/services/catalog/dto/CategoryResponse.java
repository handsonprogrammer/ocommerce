package com.ocommerce.services.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Category response DTO for API responses
 */
@Schema(description = "Category information")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Schema(description = "Category path in hierarchy", example = "/electronics/computers")
    @JsonProperty("path")
    private String path;

    @Schema(description = "Whether category is active", example = "true")
    @JsonProperty("isActive")
    private boolean isActive;

    @Schema(description = "Number of products in category", example = "42")
    @JsonProperty("productCount")
    private Long productCount;

    @Schema(description = "Category creation timestamp")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "Category last update timestamp")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    /**
     * SEO metadata response DTO
     */
    @Schema(description = "SEO metadata information")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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
    }
}
