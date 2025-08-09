package com.ocommerce.services.catalog.domain;

import com.ocommerce.services.catalog.constants.ProductStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product document for MongoDB catalog domain
 */
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

    @Field("category_ids")
    @Indexed
    private List<UUID> categoryIds;

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

    // Constructors
    public Product() {
        this.id = UUID.randomUUID();
        this.status = ProductStatus.DRAFT;
        this.inventoryTracking = true;
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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public List<UUID> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<UUID> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<String> getCategoryPaths() {
        return categoryPaths;
    }

    public void setCategoryPaths(List<String> categoryPaths) {
        this.categoryPaths = categoryPaths;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }

    public SeoMetadata getSeoMetadata() {
        return seoMetadata;
    }

    public void setSeoMetadata(SeoMetadata seoMetadata) {
        this.seoMetadata = seoMetadata;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public boolean isInventoryTracking() {
        return inventoryTracking;
    }

    public void setInventoryTracking(boolean inventoryTracking) {
        this.inventoryTracking = inventoryTracking;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public ProductDimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(ProductDimensions dimensions) {
        this.dimensions = dimensions;
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

    /**
     * Embedded SEO metadata
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

    /**
     * Embedded product dimensions
     */
    public static class ProductDimensions {
        @Field("length")
        private Double length;

        @Field("width")
        private Double width;

        @Field("height")
        private Double height;

        @Field("unit")
        private String unit; // cm, inch, etc.

        // Constructors, getters, and setters
        public ProductDimensions() {}

        public ProductDimensions(Double length, Double width, Double height, String unit) {
            this.length = length;
            this.width = width;
            this.height = height;
            this.unit = unit;
        }

        public Double getLength() {
            return length;
        }

        public void setLength(Double length) {
            this.length = length;
        }

        public Double getWidth() {
            return width;
        }

        public void setWidth(Double width) {
            this.width = width;
        }

        public Double getHeight() {
            return height;
        }

        public void setHeight(Double height) {
            this.height = height;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}
