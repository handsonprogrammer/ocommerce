package com.ocommerce.services.cart.service;

import com.ocommerce.services.cart.dto.ProductPricingInfo;
import com.ocommerce.services.cart.exception.ProductValidationException;
import com.ocommerce.services.catalog.dto.ProductResponse;
import com.ocommerce.services.catalog.dto.ProductVariantResponse;
import com.ocommerce.services.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for validating products and fetching pricing information from catalog domain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductValidationService {

    private final ProductService productService;

    /**
     * Validate product and get pricing information
     * @param productId Product ID to validate
     * @param variantId Optional variant ID
     * @return ProductPricingInfo with current pricing data
     * @throws ProductValidationException if product is invalid
     */
    public ProductPricingInfo validateAndGetProductPricing(UUID productId, UUID variantId) {
        log.info("Validating product {} with variant {}", productId, variantId);

        // Get product from catalog service
        ProductResponse productResponse = productService.getProductById(productId)
            .orElseThrow(() -> new ProductValidationException("Product not found: " + productId));

        // Check if product is active
        if (!"ACTIVE".equals(productResponse.getStatus())) {
            throw new ProductValidationException("Product is not active: " + productId);
        }

        // If variant is specified, validate it
        if (variantId != null) {
            return validateVariantAndGetPricing(productResponse, variantId);
        } else {
            return getProductBasePricing(productResponse);
        }
    }

    private ProductPricingInfo validateVariantAndGetPricing(ProductResponse productResponse, UUID variantId) {
        log.info("Validating variant {}", variantId);

        if (productResponse.getVariants() == null || productResponse.getVariants().isEmpty()) {
            throw new ProductValidationException("Product has no variants: " + productResponse.getId());
        }

        ProductVariantResponse variant = productResponse.getVariants().stream()
            .filter(v -> variantId.equals(v.getVariantId()))
            .findFirst()
            .orElseThrow(() -> new ProductValidationException("Variant not found: " + variantId));

        // Check variant status if available (assuming status field exists)
        // Note: Based on the DTO, status might not be exposed in response

        return ProductPricingInfo.builder()
            .productId(productResponse.getId())
            .variantId(variant.getVariantId())
            .productName(productResponse.getName())
            .variantName(variant.getVariantName())
            .sku(variant.getSku())
            .price(variant.getPrice() != null ? BigDecimal.valueOf(variant.getPrice()) : BigDecimal.ZERO)
            .compareAtPrice(variant.getCompareAtPrice())
            .unitOfMeasure(productResponse.getUnitOfMeasure())
            .inventoryTracking(productResponse.isInventoryTracking())
            .availableStock(getVariantStock(variant)) // Will implement based on inventory structure
            .isActive(true) // Variant is considered active if product is active
            .build();
    }

    private ProductPricingInfo getProductBasePricing(ProductResponse productResponse) {
        log.info("Getting base product pricing for {}", productResponse.getId());

        return ProductPricingInfo.builder()
            .productId(productResponse.getId())
            .variantId(null)
            .productName(productResponse.getName())
            .variantName(null)
            .sku(null) // Base product might not have SKU
            .price(productResponse.getBasePrice() != null ? BigDecimal.valueOf(productResponse.getBasePrice()) : BigDecimal.ZERO)
            .compareAtPrice(null)
            .unitOfMeasure(productResponse.getUnitOfMeasure())
            .inventoryTracking(productResponse.isInventoryTracking())
            .availableStock(getProductStock(productResponse)) // Will implement based on inventory structure
            .isActive(true) // Already validated above
            .build();
    }

    private Integer getVariantStock(ProductVariantResponse variant) {
        // TODO: Implement based on your inventory structure in ProductVariantResponse
        // For now, return a default value
        log.debug("Getting variant stock for SKU: {}", variant.getSku());
        return Integer.MAX_VALUE; // Unlimited stock for now
    }

    private Integer getProductStock(ProductResponse product) {
        // TODO: Implement based on your inventory structure in ProductResponse
        // For now, return a default value
        log.debug("Getting product stock for: {}", product.getName());
        return Integer.MAX_VALUE; // Unlimited stock for now
    }

    /**
     * Validate product stock availability
     * @param productId Product ID
     * @param variantId Optional variant ID
     * @param requestedQuantity Requested quantity
     * @return true if stock is available
     */
    public boolean validateStockAvailability(UUID productId, UUID variantId, int requestedQuantity) {
        log.info("Validating stock for product {} variant {} quantity {}", productId, variantId, requestedQuantity);

        try {
            // Get product pricing info (which includes stock info)
            ProductPricingInfo pricingInfo = validateAndGetProductPricing(productId, variantId);

            if (!pricingInfo.isInventoryTracking()) {
                return true; // No inventory tracking means unlimited stock
            }

            return pricingInfo.getAvailableStock() >= requestedQuantity;
        } catch (ProductValidationException e) {
            log.warn("Product validation failed during stock check: {}", e.getMessage());
            return false;
        }
    }
}
