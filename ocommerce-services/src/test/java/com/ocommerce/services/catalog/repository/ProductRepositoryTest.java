package com.ocommerce.services.catalog.repository;

import com.ocommerce.services.catalog.constants.ProductStatus;
import com.ocommerce.services.catalog.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setStatus(ProductStatus.ACTIVE);
        product.setBasePrice(Double.valueOf(100)); // Use integer value for reliable matching
        product.setInventoryTracking(true);
        productRepository.save(product);
    }

    @Test
    void testFindByStatusAndInventoryTrackingTrue() {
        var page = productRepository.findByStatusAndInventoryTrackingTrue(ProductStatus.ACTIVE, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Test Product");
    }

    @Test
    void testFindByNameContainingIgnoreCaseAndActive() {
        var page = productRepository.findByNameContainingIgnoreCaseAndActive("test", PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void testFindByStatus() {
        List<Product> products = productRepository.findByStatus(ProductStatus.ACTIVE);
        assertThat(products).hasSize(1);
    }

    @Test
    void testFindByCreatedBy() {
        product.setCreatedBy(UUID.randomUUID());
        productRepository.save(product);
        var page = productRepository.findByCreatedBy(product.getCreatedBy(), PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void testFindByPriceRangeAndActive() {
        List allProducts =  productRepository.findAll();
        for (int i = 0; i < allProducts.size(); i++) {
            Product p = (Product) allProducts.get(i);
            System.out.println(p.getBasePrice());
            if (p.getBasePrice().compareTo(Double.parseDouble("50")) < 0 || p.getBasePrice().compareTo(Double.parseDouble("150")) > 0) {
                p.setStatus(ProductStatus.INACTIVE);
                productRepository.save(p);
            }
        }

        var page = productRepository.findByBasePriceBetweenAndStatus(
                Double.parseDouble("50"),
                Double.parseDouble("150"),
            ProductStatus.ACTIVE,
            PageRequest.of(0, 10)
        );
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void testFindBySlugAndActive() {
        product.setSeoMetadata(new Product.SeoMetadata());
        product.getSeoMetadata().setSlug("test-slug");
        productRepository.save(product);
        Optional<Product> found = productRepository.findBySlugAndActive("test-slug");
        assertThat(found).isPresent();
    }
}
