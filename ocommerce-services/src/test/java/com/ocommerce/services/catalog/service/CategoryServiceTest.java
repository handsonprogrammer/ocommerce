package com.ocommerce.services.catalog.service;

import com.ocommerce.services.catalog.domain.Category;
import com.ocommerce.services.catalog.dto.CategoryResponse;
import com.ocommerce.services.catalog.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CategoryService
 */

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private Category testRootCategory;
    private Category testChildCategory;

    @BeforeEach
    void setUp() {
        testRootCategory = createTestRootCategory();
        testChildCategory = createTestChildCategory(testRootCategory);
        testCategory = testRootCategory;
    }

    @Test
    void getRootCategories_ShouldReturnRootCategories() {
        // Given
        List<Category> rootCategories = List.of(testRootCategory);
        when(categoryRepository.findByParentIdIsNullAndIsActiveTrue()).thenReturn(rootCategories);

        // When
        List<CategoryResponse> result = categoryService.getRootCategories();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
        assertThat(result.get(0).getParentId()).isNull();
        verify(categoryRepository).findByParentIdIsNullAndIsActiveTrue();
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategory() {
        // Given
        UUID categoryId = testCategory.getId();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // When
        Optional<CategoryResponse> result = categoryService.getCategoryById(categoryId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(categoryId);
        assertThat(result.get().getName()).isEqualTo("Electronics");
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void getCategoryById_WhenCategoryNotExists_ShouldReturnEmpty() {
        // Given
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When
        Optional<CategoryResponse> result = categoryService.getCategoryById(categoryId);

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void getCategoryBySlug_WhenCategoryExists_ShouldReturnCategory() {
        // Given
        String slug = "electronics";
        when(categoryRepository.findBySlugAndActive(slug)).thenReturn(Optional.of(testCategory));

        // When
        Optional<CategoryResponse> result = categoryService.getCategoryBySlug(slug);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
        verify(categoryRepository).findBySlugAndActive(slug);
    }

    @Test
    void getChildCategories_ShouldReturnChildCategories() {
        // Given
        UUID parentId = testRootCategory.getId();
        List<Category> childCategories = List.of(testChildCategory);
        when(categoryRepository.findByParentIdAndIsActiveTrue(parentId)).thenReturn(childCategories);

        // When
        List<CategoryResponse> result = categoryService.getChildCategories(parentId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Computers");
        assertThat(result.get(0).getParentId()).isEqualTo(parentId);
        verify(categoryRepository).findByParentIdAndIsActiveTrue(parentId);
    }

    @Test
    void getAllCategories_ShouldReturnAllActiveCategories() {
        // Given
        List<Category> categories = List.of(testRootCategory, testChildCategory);
        when(categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()).thenReturn(categories);

        // When
        List<CategoryResponse> result = categoryService.getAllCategories();

        // Then
        assertThat(result).hasSize(2);
        verify(categoryRepository).findByIsActiveTrueOrderBySortOrderAsc();
    }

    @Test
    void getCategoriesByLevel_ShouldReturnCategoriesAtSpecificLevel() {
        // Given
        Integer level = 0;
        List<Category> categories = List.of(testRootCategory);
        when(categoryRepository.findByLevelAndIsActiveTrue(level)).thenReturn(categories);

        // When
        List<CategoryResponse> result = categoryService.getCategoriesByLevel(level);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLevel()).isEqualTo(0);
        verify(categoryRepository).findByLevelAndIsActiveTrue(level);
    }

    @Test
    void searchCategoriesByName_ShouldReturnMatchingCategories() {
        // Given
        String searchTerm = "electronics";
        List<Category> categories = List.of(testRootCategory);
        when(categoryRepository.findByNameContainingIgnoreCaseAndActive(searchTerm)).thenReturn(categories);

        // When
        List<CategoryResponse> result = categoryService.searchCategoriesByName(searchTerm);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase(searchTerm);
        verify(categoryRepository).findByNameContainingIgnoreCaseAndActive(searchTerm);
    }

    @Test
    void textSearchCategories_ShouldReturnMatchingCategories() {
        // Given
        String searchText = "electronic devices";
        List<Category> categories = List.of(testRootCategory);
        when(categoryRepository.findByTextSearch(searchText)).thenReturn(categories);

        // When
        List<CategoryResponse> result = categoryService.textSearchCategories(searchText);

        // Then
        assertThat(result).hasSize(1);
        verify(categoryRepository).findByTextSearch(searchText);
    }

    @Test
    void getCategoryTree_ShouldReturnHierarchicalStructure() {
        // Given
        List<Category> rootCategories = List.of(testRootCategory);
        List<Category> childCategories = List.of(testChildCategory);

        when(categoryRepository.findByParentIdIsNullAndIsActiveTrue()).thenReturn(rootCategories);
        when(categoryRepository.findByParentIdAndIsActiveTrue(testRootCategory.getId())).thenReturn(childCategories);

        // When
        List<CategoryService.CategoryTreeResponse> result = categoryService.getCategoryTree();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getName()).isEqualTo("Computers");

        verify(categoryRepository).findByParentIdIsNullAndIsActiveTrue();
        verify(categoryRepository).findByParentIdAndIsActiveTrue(testRootCategory.getId());
    }

    @Test
    void getCategoriesWithMinProducts_ShouldReturnCategoriesWithProductCount() {
        // Given
        Long minProductCount = 5L;
        List<Category> categories = List.of(testRootCategory);
        when(categoryRepository.findByProductCountGreaterThanAndIsActiveTrue(minProductCount)).thenReturn(categories);

        // When
        List<CategoryResponse> result = categoryService.getCategoriesWithMinProducts(minProductCount);

        // Then
        assertThat(result).hasSize(1);
        verify(categoryRepository).findByProductCountGreaterThanAndIsActiveTrue(minProductCount);
    }

    @Test
    void getActiveCategoriesCount_ShouldReturnCount() {
        // Given
        long expectedCount = 10L;
        when(categoryRepository.countByIsActiveTrue()).thenReturn(expectedCount);

        // When
        long result = categoryService.getActiveCategoriesCount();

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(categoryRepository).countByIsActiveTrue();
    }

    // Helper methods
    private Category createTestRootCategory() {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Electronics");
        category.setDescription("Electronic devices and accessories");
        category.setLevel(0);
        category.setPath("/electronics");
        category.setActive(true);
        category.setProductCount(25L);
        category.setSortOrder(1);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        // Add SEO metadata
        Category.SeoMetadata seoMetadata = new Category.SeoMetadata();
        seoMetadata.setSlug("electronics");
        seoMetadata.setMetaTitle("Electronics - Best Deals Online");
        seoMetadata.setMetaDescription("Shop the latest electronics with great deals");
        category.setSeoMetadata(seoMetadata);

        // Add child IDs
        category.setChildren(List.of(createTestChildCategory(null)));

        return category;
    }

    private Category createTestChildCategory(Category parent) {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Computers");
        category.setDescription("Desktop and laptop computers");
        category.setParent(parent);
        category.setLevel(1);
        category.setPath("/electronics/computers");
        category.setActive(true);
        category.setProductCount(10L);
        category.setSortOrder(1);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        // Add SEO metadata
        Category.SeoMetadata seoMetadata = new Category.SeoMetadata();
        seoMetadata.setSlug("computers");
        seoMetadata.setMetaTitle("Computers - Latest Models");
        seoMetadata.setMetaDescription("Shop the latest desktop and laptop computers");
        category.setSeoMetadata(seoMetadata);

        return category;
    }
}
