package com.ocommerce.services.catalog.repository;

import com.ocommerce.services.catalog.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest()
@ExtendWith(SpringExtension.class)
@ActiveProfiles("integration-test")
class CategoryRepositoryIT {

    @Autowired
    private CategoryRepository categoryRepository;

    private Category rootCategory;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        rootCategory = new Category();
        rootCategory.setId(UUID.randomUUID());
        rootCategory.setName("Root Category");
        rootCategory.setActive(true);
        rootCategory.setLevel(0);
        rootCategory.setSortOrder(1);
        categoryRepository.save(rootCategory);
    }

    @Test
    void testFindByParentIdIsNullAndIsActiveTrue() {
        List<Category> roots = categoryRepository.findByParentIdIsNullAndIsActiveTrue();
        assertThat(roots).hasSize(1);
    }

    @Test
    void testFindByLevelAndIsActiveTrue() {
        List<Category> levels = categoryRepository.findByLevelAndIsActiveTrue(0);
        assertThat(levels).hasSize(1);
    }

    @Test
    void testFindByNameContainingIgnoreCaseAndActive() {
        List<Category> found = categoryRepository.findByNameContainingIgnoreCaseAndActive("root");
        assertThat(found).hasSize(1);
    }

    @Test
    void testFindByIsActiveTrueOrderBySortOrderAsc() {
        List<Category> found = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        assertThat(found).hasSize(1);
    }

    @Test
    void testFindBySlugAndActive() {
        rootCategory.setSeoMetadata(new Category.SeoMetadata());
        rootCategory.getSeoMetadata().setSlug("root-slug");
        categoryRepository.save(rootCategory);
        Optional<Category> found = categoryRepository.findBySlugAndActive("root-slug");
        assertThat(found).isPresent();
    }
}

