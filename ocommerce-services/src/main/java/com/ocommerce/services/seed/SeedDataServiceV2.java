package com.ocommerce.services.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocommerce.services.catalog.constants.ProductStatus;
import com.ocommerce.services.catalog.domain.Category;
import com.ocommerce.services.catalog.domain.Product;
import com.ocommerce.services.catalog.domain.ProductVariant;
import com.ocommerce.services.catalog.repository.CategoryRepository;
import com.ocommerce.services.catalog.repository.ProductRepository;
import com.ocommerce.services.catalog.service.CategoryPathService;
import com.ocommerce.services.user.domain.Address;
import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.repository.AddressRepository;
import com.ocommerce.services.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * To run this seeder application:
 * 1. Make sure your Spring Boot application is started with the 'dev' profile:
 *      - From command line: ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
 *      - Or set SPRING_PROFILES_ACTIVE=dev in your environment.
 * 2. The interactive seed menu will appear in the application logs/console.
 * 3. Follow the prompts to load, clear, or inspect seed data.
 */

/**
 * Interactive seed data service that provides menu options for loading different types of data
 */
@Component
@Profile("dev")
public class SeedDataServiceV2 implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SeedDataServiceV2.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryPathService categoryPathService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting interactive seed data service...");
        showMainMenu();
    }

    private void showMainMenu() {
        while (true) {
            System.out.println("\n=== SEED DATA SERVICE ===");
            System.out.println("1. Load All Data");
            System.out.println("2. Load Users");
            System.out.println("3. Load Categories");
            System.out.println("4. Load Products");
            System.out.println("5. Load Product Variants");
            System.out.println("6. Load Apple Products");
            System.out.println("7. Load Apple Variants");
            System.out.println("8. Show Data Statistics");
            System.out.println("9. Clear All Data");
            System.out.println("0. Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> loadAllData();
                    case "2" -> seedUsers();
                    case "3" -> seedCategories();
                    case "4" -> seedProducts();
                    case "5" -> seedProductVariants();
                    case "6" -> showAppleProductsMenu();
                    case "7" -> showAppleVariantsMenu();
                    case "8" -> showDataStatistics();
                    case "9" -> clearAllData();
                    case "0" -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                logger.error("Error executing operation: {}", e.getMessage());
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showAppleProductsMenu() throws IOException {
        List<String> appleProductFiles = getAppleProductFiles();

        System.out.println("\n=== APPLE PRODUCTS ===");
        System.out.println("0. Back to main menu");
        System.out.println("A. Load All Apple Products");

        for (int i = 0; i < appleProductFiles.size(); i++) {
            String fileName = appleProductFiles.get(i);
            String year = extractYearFromFileName(fileName);
            System.out.println((i + 1) + ". Load Apple Products " + year);
        }

        System.out.print("Select an option: ");
        String choice = scanner.nextLine().trim();

        if ("0".equals(choice)) {
            return;
        } else if ("A".equalsIgnoreCase(choice)) {
            loadAllAppleProducts();
        } else {
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < appleProductFiles.size()) {
                    String filePath = "apple/" + appleProductFiles.get(index);
                    int count = loadAppleProductsFromFile(filePath);
                    System.out.println("Loaded " + count + " products from " + appleProductFiles.get(index));
                } else {
                    System.out.println("Invalid option.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid option.");
            }
        }
    }

    private void showAppleVariantsMenu() throws IOException {
        List<String> appleVariantFiles = getAppleVariantFiles();

        System.out.println("\n=== APPLE VARIANTS ===");
        System.out.println("0. Back to main menu");
        System.out.println("A. Load All Apple Variants");

        for (int i = 0; i < appleVariantFiles.size(); i++) {
            String fileName = appleVariantFiles.get(i);
            String year = extractYearFromFileName(fileName);
            System.out.println((i + 1) + ". Load Apple Variants " + year);
        }

        System.out.print("Select an option: ");
        String choice = scanner.nextLine().trim();

        if ("0".equals(choice)) {
            return;
        } else if ("A".equalsIgnoreCase(choice)) {
            loadAllAppleVariants();
        } else {
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < appleVariantFiles.size()) {
                    String filePath = "apple/" + appleVariantFiles.get(index);
                    int count = loadAppleVariantsFromFile(filePath);
                    System.out.println("Loaded " + count + " variants from " + appleVariantFiles.get(index));
                } else {
                    System.out.println("Invalid option.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid option.");
            }
        }
    }

    private List<String> getAppleProductFiles() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:seed-data/apple/apple-products-*.json");

        List<String> files = new ArrayList<>();
        for (Resource resource : resources) {
            files.add(resource.getFilename());
        }
        Collections.sort(files);
        return files;
    }

    private List<String> getAppleVariantFiles() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:seed-data/apple/apple-variants-*.json");

        List<String> files = new ArrayList<>();
        for (Resource resource : resources) {
            files.add(resource.getFilename());
        }
        Collections.sort(files);
        return files;
    }

    private String extractYearFromFileName(String fileName) {
        // Extract year from file names like "apple-products-2011.json"
        String[] parts = fileName.split("-");
        if (parts.length >= 3) {
            return parts[2].replace(".json", "");
        }
        return "Unknown";
    }

    private void loadAllData() throws Exception {
        System.out.println("Loading all seed data...");
        seedUsers();
        seedCategories();
        seedProducts();
        seedProductVariants();
        loadAllAppleProducts();
        loadAllAppleVariants();
        System.out.println("All data loaded successfully!");
    }

    private void loadAllAppleProducts() throws IOException {
        System.out.println("Loading all Apple products...");
        List<String> files = getAppleProductFiles();
        int totalLoaded = 0;

        for (String file : files) {
            String filePath = "apple/" + file;
            int count = loadAppleProductsFromFile(filePath);
            totalLoaded += count;
            System.out.println("Loaded " + count + " products from " + file);
        }

        System.out.println("Total Apple products loaded: " + totalLoaded);
    }

    private void loadAllAppleVariants() throws IOException {
        System.out.println("Loading all Apple variants...");
        List<String> files = getAppleVariantFiles();
        int totalLoaded = 0;

        for (String file : files) {
            String filePath = "apple/" + file;
            int count = loadAppleVariantsFromFile(filePath);
            totalLoaded += count;
            System.out.println("Loaded " + count + " variants from " + file);
        }

        System.out.println("Total Apple variants loaded: " + totalLoaded);
    }

    private int loadAppleProductsFromFile(String filePath) {
        try {
            ClassPathResource resource = new ClassPathResource("seed-data/" + filePath);
            if (!resource.exists()) {
                System.out.println("File not found: " + filePath);
                return 0;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                List<Map<String, Object>> products = objectMapper.readValue(
                    inputStream, new TypeReference<List<Map<String, Object>>>() {}
                );

                int loadedCount = 0;
                for (Map<String, Object> productData : products) {
                    Product product = mapToProduct(productData);
                    if (product != null) {
                        productRepository.save(product);
                        loadedCount++;
                    }
                }
                return loadedCount;
            }
        } catch (Exception e) {
            logger.error("Error loading Apple products from {}: {}", filePath, e.getMessage());
            System.out.println("Error loading file: " + e.getMessage());
            return 0;
        }
    }

    private int loadAppleVariantsFromFile(String filePath) {
        try {
            ClassPathResource resource = new ClassPathResource("seed-data/" + filePath);
            if (!resource.exists()) {
                System.out.println("File not found: " + filePath);
                return 0;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                List<Map<String, Object>> variants = objectMapper.readValue(
                    inputStream, new TypeReference<List<Map<String, Object>>>() {}
                );

                int loadedCount = 0;
                for (Map<String, Object> variantData : variants) {
                    String productName = (String) variantData.get("productName");
                    if (productName != null) {
                        Optional<Product> productOpt = findProductByName(productName);
                        if (productOpt.isPresent()) {
                            Product product = productOpt.get();
                            ProductVariant variant = mapToProductVariant(variantData);
                            if (variant != null) {
                                // Add variant to product's variants list
                                if (product.getVariants() == null) {
                                    product.setVariants(new ArrayList<>());
                                }
                                product.getVariants().add(variant);
                                productRepository.save(product);
                                loadedCount++;
                            }
                        } else {
                            logger.warn("Product not found for variant: {}", productName);
                        }
                    }
                }
                return loadedCount;
            }
        } catch (Exception e) {
            logger.error("Error loading Apple variants from {}: {}", filePath, e.getMessage());
            System.out.println("Error loading file: " + e.getMessage());
            return 0;
        }
    }

    private Optional<Product> findProductByName(String productName) {
        List<Product> products = productRepository.findByStatus(ProductStatus.ACTIVE);
        return products.stream()
            .filter(p -> productName.equals(p.getName()))
            .findFirst();
    }

    private void showDataStatistics() {
        System.out.println("\n=== DATA STATISTICS ===");
        System.out.println("Users: " + userRepository.count());
        System.out.println("Addresses: " + addressRepository.count());
        System.out.println("Categories: " + categoryRepository.count());
        System.out.println("Products: " + productRepository.count());

        // Count total variants across all products
        long totalVariants = productRepository.findAll().stream()
            .mapToLong(product -> product.getVariants() != null ? product.getVariants().size() : 0)
            .sum();
        System.out.println("Product Variants: " + totalVariants);
    }

    private void clearAllData() {
        System.out.print("Are you sure you want to clear all data? (yes/no): ");
        String confirmation = scanner.nextLine().trim();

        if ("yes".equalsIgnoreCase(confirmation)) {
            System.out.println("Clearing all data...");
            productRepository.deleteAll();
            categoryRepository.deleteAll();
            addressRepository.deleteAll();
            userRepository.deleteAll();
            System.out.println("All data cleared successfully!");
        } else {
            System.out.println("Operation cancelled.");
        }
    }

    private void seedUsers() throws IOException {
        if (userRepository.count() > 0) {
            System.out.print("Users already exist. Overwrite? (yes/no): ");
            String response = scanner.nextLine().trim();
            if (!"yes".equalsIgnoreCase(response)) {
                System.out.println("Skipping users seed.");
                return;
            }
            userRepository.deleteAll();
        }

        logger.info("Seeding user data...");
        ClassPathResource resource = new ClassPathResource("seed-data/users.json");

        try (InputStream inputStream = resource.getInputStream()) {
            List<UserSeedData> users = objectMapper.readValue(
                inputStream, new TypeReference<List<UserSeedData>>() {}
            );

            for (UserSeedData userData : users) {
                User user = new User();
                user.setFirstName(userData.firstName);
                user.setLastName(userData.lastName);
                user.setEmail(userData.email);
                user.setPassword(passwordEncoder.encode(userData.password));
                user.setPhoneNumber(userData.phoneNumber);
                user.setEmailVerified(userData.emailVerified != null ? userData.emailVerified : false);
                user.setAccountLocked(userData.accountLocked != null ? userData.accountLocked : false);

                User savedUser = userRepository.save(user);

                // Create addresses if provided
                if (userData.addresses != null) {
                    for (AddressSeedData addressData : userData.addresses) {
                        Address address = new Address();
                        address.setUser(savedUser);
                        address.setType(addressData.type);
                        address.setStreetAddress(addressData.streetAddress);
                        address.setAddressLine2(addressData.addressLine2);
                        address.setCity(addressData.city);
                        address.setState(addressData.state);
                        address.setPostalCode(addressData.postalCode);
                        address.setCountry(addressData.country);
                        address.setDefault(addressData.isDefault != null ? addressData.isDefault : false);
                        address.setDeleted(addressData.isDeleted != null ? addressData.isDeleted : false);

                        addressRepository.save(address);
                    }
                }
            }

            System.out.println("Loaded " + users.size() + " users with addresses.");
        }
    }

    private void seedCategories() throws IOException {
        ObjectMapper objectMapper = this.objectMapper;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (categoryRepository.count() > 0) {
            System.out.print("Categories already exist. Overwrite? (yes/no): ");
            String response = scanner.nextLine().trim();
            if (!"yes".equalsIgnoreCase(response)) {
                System.out.println("Skipping categories seed.");
                return;
            }
            categoryRepository.deleteAll();
        }

        logger.info("Seeding category data...");
        ClassPathResource resource = new ClassPathResource("seed-data/categories.json");

        try (InputStream inputStream = resource.getInputStream()) {
            List<CategorySeedData> categories = objectMapper.readValue(
                inputStream, new TypeReference<List<CategorySeedData>>() {}
            );

            for (CategorySeedData categoryData : categories) {
                Category category = new Category();
                category.setName(categoryData.name);
                category.setDescription(categoryData.description);
                category.setParentId(categoryData.parentId);
                category.setLevel(categoryData.level);
                category.setPath(categoryData.path);
                category.setSortOrder(categoryData.sortOrder);
                category.setActive(categoryData.active != null ? categoryData.active : true);
                category.setProductCount(categoryData.productCount != null ? categoryData.productCount : 0L);

                if (categoryData.seoMetadata != null) {
                    Category.SeoMetadata seoMetadata = new Category.SeoMetadata();
                    seoMetadata.setSlug(categoryData.seoMetadata.slug);
                    seoMetadata.setMetaTitle(categoryData.seoMetadata.metaTitle);
                    seoMetadata.setMetaDescription(categoryData.seoMetadata.metaDescription);
                    category.setSeoMetadata(seoMetadata);
                }

                categoryRepository.save(category);
            }

            System.out.println("Loaded " + categories.size() + " categories.");
        }
    }

    private void seedProducts() throws IOException {
        if (productRepository.count() > 0) {
            System.out.print("Products already exist. Overwrite? (yes/no): ");
            String response = scanner.nextLine().trim();
            if (!"yes".equalsIgnoreCase(response)) {
                System.out.println("Skipping products seed.");
                return;
            }
            productRepository.deleteAll();
        }

        logger.info("Seeding product data...");
        ClassPathResource resource = new ClassPathResource("seed-data/products.json");

        try (InputStream inputStream = resource.getInputStream()) {
            List<Map<String, Object>> products = objectMapper.readValue(
                inputStream, new TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> productData : products) {
                Product product = mapToProduct(productData);
                if (product != null) {
                    productRepository.save(product);
                }
            }

            System.out.println("Loaded " + products.size() + " products.");
        }
    }

    private void seedProductVariants() throws IOException {
        logger.info("Seeding product variant data...");
        ClassPathResource resource = new ClassPathResource("seed-data/variants.json");

        if (!resource.exists()) {
            System.out.println("Variants file not found, skipping.");
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            List<Map<String, Object>> variants = objectMapper.readValue(
                inputStream, new TypeReference<List<Map<String, Object>>>() {}
            );

            int loadedCount = 0;
            for (Map<String, Object> variantData : variants) {
                String productName = (String) variantData.get("productName");
                if (productName != null) {
                    Optional<Product> productOpt = findProductByName(productName);
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        ProductVariant variant = mapToProductVariant(variantData);
                        if (variant != null) {
                            if (product.getVariants() == null) {
                                product.setVariants(new ArrayList<>());
                            }
                            product.getVariants().add(variant);
                            productRepository.save(product);
                            loadedCount++;
                        }
                    }
                }
            }

            System.out.println("Loaded " + loadedCount + " product variants.");
        }
    }

    private ProductVariant mapToProductVariant(Map<String, Object> variantData) {
        try {
            ProductVariant variant = new ProductVariant();

            variant.setSku((String) variantData.get("sku"));
            variant.setVariantName((String) variantData.get("variantName"));
            variant.setBarcode((String) variantData.get("barcode"));

            // Set prices
            if (variantData.get("price") != null) {
                variant.setPrice(getDoubleValue(variantData.get("price")));
            }
            if (variantData.get("compareAtPrice") != null) {
                variant.setCompareAtPrice(new BigDecimal(variantData.get("compareAtPrice").toString()));
            }
            if (variantData.get("costPrice") != null) {
                variant.setCostPrice(new BigDecimal(variantData.get("costPrice").toString()));
            }

            // Set position
            if (variantData.get("position") != null) {
                variant.setPosition(getIntegerValue(variantData.get("position")));
            }

            // Set attributes
            @SuppressWarnings("unchecked")
            Map<String, Object> attributesObj = (Map<String, Object>) variantData.get("attributes");
            if (attributesObj != null) {
                Map<String, String> attributes = new HashMap<>();
                for (Map.Entry<String, Object> entry : attributesObj.entrySet()) {
                    if (entry.getValue() != null) {
                        attributes.put(entry.getKey(), entry.getValue().toString());
                    }
                }
                variant.setAttributes(attributes);
            }

            // Set image URLs
            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) variantData.get("imageUrls");
            if (imageUrls != null) {
                variant.setImageUrls(imageUrls);
            }

            // Set weight
            if (variantData.get("weight") != null) {
                variant.setWeight(getDoubleValue(variantData.get("weight")));
            }

            // Set dimensions
            @SuppressWarnings("unchecked")
            Map<String, Object> dimensionsData = (Map<String, Object>) variantData.get("dimensions");
            if (dimensionsData != null) {
                Product.ProductDimensions dimensions = new Product.ProductDimensions();
                dimensions.setLength(getDoubleValue(dimensionsData.get("length")));
                dimensions.setWidth(getDoubleValue(dimensionsData.get("width")));
                dimensions.setHeight(getDoubleValue(dimensionsData.get("height")));
                dimensions.setUnit((String) dimensionsData.get("unit"));
                variant.setDimensions(dimensions);
            }

            // Set inventory
            @SuppressWarnings("unchecked")
            Map<String, Object> inventoryData = (Map<String, Object>) variantData.get("inventory");
            if (inventoryData != null) {
                ProductVariant.VariantInventory inventory = new ProductVariant.VariantInventory();
                inventory.setQuantity(getIntegerValue(inventoryData.get("quantity")));
                inventory.setLowStockThreshold(getIntegerValue(inventoryData.get("lowStockThreshold")));
                inventory.setTrackInventory(getBooleanValue(inventoryData.get("trackInventory")));
                inventory.setAllowBackorder(getBooleanValue(inventoryData.get("allowBackorder")));
                variant.setInventory(inventory);
            }

            return variant;
        } catch (Exception e) {
            logger.error("Error mapping variant data: {}", e.getMessage());
            return null;
        }
    }

    private Product mapToProduct(Map<String, Object> productData) {
        try {
            Product product = new Product();
            product.setName((String) productData.get("name"));
            product.setShortDescription((String) productData.get("shortDescription"));
            product.setLongDescription((String) productData.get("longDescription"));
            product.setThumbnailUrl((String) productData.get("thumbnailUrl"));

            if (productData.get("basePrice") != null) {
                product.setBasePrice(getDoubleValue(productData.get("basePrice")));
            }

            product.setUnitOfMeasure((String) productData.get("unitOfMeasure"));

            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) productData.get("imageUrls");
            if (imageUrls != null) {
                product.setImageUrls(imageUrls);
            }

            if (productData.get("status") != null) {
                product.setStatus(ProductStatus.valueOf(productData.get("status").toString()));
            }

            return product;
        } catch (Exception e) {
            logger.error("Error mapping product data: {}", e.getMessage());
            return null;
        }
    }

    private Double getDoubleValue(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : null;
    }

    private Integer getIntegerValue(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    private Boolean getBooleanValue(Object value) {
        return value instanceof Boolean ? (Boolean) value : null;
    }

    // Seed data classes
    static class UserSeedData {
        public String firstName;
        public String lastName;
        public String email;
        public String password;
        public String phoneNumber;
        public Boolean emailVerified;
        public Boolean accountLocked;
        public List<AddressSeedData> addresses;
    }

    static class AddressSeedData {
        public String type;
        public String streetAddress;
        public String addressLine2;
        public String city;
        public String state;
        public String postalCode;
        public String country;
        public Boolean isDefault;
        public Boolean isDeleted;
    }

    static class CategorySeedData {
        public String name;
        public String description;
        public String thumbnailUrl;
        public UUID parentId;
        public Integer level;
        public String path;
        public Integer sortOrder;
        public Boolean active;
        public Long productCount;
        public SeoMetadataSeedData seoMetadata;
    }

    static class SeoMetadataSeedData {
        public String slug;
        public String metaTitle;
        public String metaDescription;
    }
}

