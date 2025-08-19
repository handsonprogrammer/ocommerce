package com.ocommerce.services.catalog.domain;

import com.ocommerce.services.catalog.constants.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product document for MongoDB catalog domain
 */
@Data
@NoArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private UUID id;

    @TextIndexed(weight = 2)
    @Field("name")
    private String name;

    @TextIndexed
    @Field("short_description")
    private String shortDescription;

    @TextIndexed
    @Field("long_description")
    private String longDescription;

    @Field("thumbnail_url")
    private String thumbnailUrl;

    @Field("image_urls")
    private List<String> imageUrls;

    @Indexed
    @Field("base_price")
    private Double basePrice;

    @Field("unit_of_measure")
    private String unitOfMeasure;

    // Use MongoDB reference for categories
    @DBRef
    @Field("categories")
    @Indexed
    private List<Category> categories;

    @Field("category_paths")
    @Indexed
    private List<String> categoryPaths; // Stores hierarchy paths like "cat1>cat2>cat3"

    @Field("variants")
    private List<ProductVariant> variants;

    @Field("seo_metadata")
    private SeoMetadata seoMetadata;

    @Field("attributes")
    private Map<String, Object> attributes;

    @Indexed
    @Field("status")
    private ProductStatus status;

    @Field("inventory_tracking")
    private boolean inventoryTracking;

    @Field("weight")
    private Double weight;

    @Field("dimensions")
    private ProductDimensions dimensions;

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

    // Custom initialization
    public void initializeDefaults() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.status == null) {
            this.status = ProductStatus.DRAFT;
        }
        this.inventoryTracking = true;
    }

    /**
     * Embedded SEO metadata
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
        @Indexed(unique = false)
        private String slug;

        @Field("canonical_url")
        private String canonicalUrl;
    }

    /**
     * Embedded product dimensions
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDimensions {
        @Field("length")
        private Double length;

        @Field("width")
        private Double width;

        @Field("height")
        private Double height;

        @Field("unit")
        private String unit; // cm, inch, etc.
    }
}
