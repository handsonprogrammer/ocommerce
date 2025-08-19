package com.ocommerce.services.catalog.service;

import com.ocommerce.services.catalog.domain.Category;
import com.ocommerce.services.catalog.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating and managing category hierarchy paths
 */
@Service
public class CategoryPathService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryPathService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Generate hierarchy paths for a list of category IDs
     * @param categories List of categories
     * @return List of hierarchy paths in format "cat1>cat2>cat3"
     */
    public List<String> generateCategoryPaths(List<Category> categories) {


        List<String> paths = new ArrayList<>();

        Map<UUID, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, category -> category));

        // Generate path for each category
        for (Category category : categories) {
            if (category != null) {
                String path = buildCategoryPath(category, categoryMap);
                if (path != null && !path.isEmpty()) {
                    paths.add(path);
                }
            }
        }

        return paths.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Generate hierarchy path for a single category ID
     * @param categoryId Category ID
     * @return Hierarchy path in format "cat1>cat2>cat3"
     */
    public String generateCategoryPath(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }

        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            return null;
        }

        Category category = categoryOpt.get();

        // If category already has a computed path, use it
        if (category.getPath() != null && !category.getPath().isEmpty()) {
            // Convert from "/electronics/computers/laptops" to "electronics>computers>laptops"
            return category.getPath().substring(1).replace("/", ">");
        }

        // Build path by traversing up the hierarchy
        return buildCategoryPathFromHierarchy(category);
    }

    /**
     * Build category path by traversing the hierarchy
     * @param category Starting category
     * @param categoryMap Map of categories for efficient lookup
     * @return Category path string
     */
    private String buildCategoryPath(Category category, Map<UUID, Category> categoryMap) {
        if (category == null) {
            return null;
        }

        // If category already has a computed path, use it
        if (category.getPath() != null && !category.getPath().isEmpty()) {
            // Convert from "/electronics/computers/laptops" to "electronics>computers>laptops"
            return category.getPath().substring(1).replace("/", ">");
        }

        // Build path by traversing up the hierarchy
        List<String> pathSegments = new ArrayList<>();
        Category current = category;
        Set<UUID> visited = new HashSet<>(); // Prevent infinite loops

        while (current != null && !visited.contains(current.getId())) {
            visited.add(current.getId());
            pathSegments.add(0, current.getName()); // Add to beginning

            if (current.getParent() != null) {
                current = categoryMap.get(current.getParent().getId());
                // If not in map, fetch from repository
                if (current == null) {
                    Optional<Category> parentOpt = categoryRepository.findById(current.getParent().getId());
                    current = parentOpt.orElse(null);
                }
            } else {
                current = null; // Reached root
            }
        }

        return pathSegments.isEmpty() ? null : String.join(">", pathSegments);
    }

    /**
     * Build category path by fetching parent categories from repository
     * @param category Starting category
     * @return Category path string
     */
    private String buildCategoryPathFromHierarchy(Category category) {
        List<String> pathSegments = new ArrayList<>();
        Category current = category;
        Set<UUID> visited = new HashSet<>(); // Prevent infinite loops

        while (current != null && !visited.contains(current.getId())) {
            visited.add(current.getId());
            pathSegments.add(0, current.getName()); // Add to beginning

            if (current.getParent() != null) {
                current = current.getParent();
            } else {
                current = null; // Reached root
            }
        }

        return pathSegments.isEmpty() ? null : String.join(">", pathSegments);
    }

    /**
     * Update category paths for all products when a category is renamed or moved
     * @param categoryId Category that was modified
     * @return List of old paths that need to be updated
     */
    public List<String> getOldPathsForCategory(UUID categoryId) {
        // This method would be used to find products that need path updates
        // when a category is modified
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Category category = categoryOpt.get();
        String currentPath = generateCategoryPath(categoryId);

        // For now, return the current path - in a real scenario,
        // we might store historical paths or compute them differently
        return currentPath != null ? List.of(currentPath) : new ArrayList<>();
    }

}
