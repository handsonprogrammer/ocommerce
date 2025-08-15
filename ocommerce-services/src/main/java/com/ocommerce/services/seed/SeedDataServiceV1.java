package com.ocommerce.services.seed;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Seed data service that reads from JSON files in resources/seed-data/
 */
//@Component
//@Profile("dev")
public class SeedDataServiceV1 implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SeedDataServiceV1.class);

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting seed data generation...");

        if (shouldSeedData()) {
            seedUsers();
            seedCategories();
            seedProducts();
            seedProductVariants();
            logger.info("Seed data generation completed successfully!");
        } else {
            logger.info("Data already exists, skipping seed data generation.");
        }
    }

    private boolean shouldSeedData() {
        return userRepository.count() == 0 && categoryRepository.count() == 0;
    }

    private void seedUsers() {
        logger.info("Seeding user data...");
        try {
            List<UserSeedData> usersData = readJsonFile("seed-data/users.json", new TypeReference<List<UserSeedData>>() {});

            for (UserSeedData userData : usersData) {
                User user = new User();
                user.setFirstName(userData.firstName);
                user.setLastName(userData.lastName);
                user.setEmail(userData.email);
                user.setPassword(passwordEncoder.encode(userData.password));
                user.setPhoneNumber(userData.phoneNumber);
                user.setEmailVerified(userData.emailVerified);
                user.setAccountLocked(userData.accountLocked);

                user = userRepository.save(user);

                // Create addresses for this user
                if (userData.addresses != null) {
                    for (AddressSeedData addressData : userData.addresses) {
                        Address address = new Address();
                        address.setUser(user);
                        address.setType(addressData.type);
                        address.setStreetAddress(addressData.streetAddress);
                        address.setAddressLine2(addressData.addressLine2);
                        address.setCity(addressData.city);
                        address.setState(addressData.state);
                        address.setPostalCode(addressData.postalCode);
                        address.setCountry(addressData.country);
                        address.setDefault(addressData.isDefault);
                        address.setDeleted(false);

                        addressRepository.save(address);
                    }
                }
            }

            logger.info("Created {} users with {} addresses", userRepository.count(), addressRepository.count());
        } catch (IOException e) {
            logger.error("Error seeding users: ", e);
        }
    }

    private void seedCategories() {
        logger.info("Seeding category data...");
        try {
            List<CategorySeedData> categoriesData = readJsonFile("seed-data/categories.json", new TypeReference<List<CategorySeedData>>() {});
            Map<String, UUID> categoryIdMap = new HashMap<>();

            // First pass: create root categories
            for (CategorySeedData categoryData : categoriesData) {
                if (categoryData.parentName == null) {
                    UUID categoryId = createCategoryFromData(categoryData, null, categoryIdMap);
                    categoryIdMap.put(categoryData.name, categoryId);
                }
            }

            // Second pass: create child categories
            for (CategorySeedData categoryData : categoriesData) {
                if (categoryData.parentName != null) {
                    UUID parentId = categoryIdMap.get(categoryData.parentName);
                    UUID categoryId = createCategoryFromData(categoryData, parentId, categoryIdMap);
                    categoryIdMap.put(categoryData.name, categoryId);
                }
            }

            logger.info("Created {} categories", categoryRepository.count());
        } catch (IOException e) {
            logger.error("Error seeding categories: ", e);
        }
    }

    private UUID createCategoryFromData(CategorySeedData data, UUID parentId, Map<String, UUID> categoryIdMap) {
        Category category = new Category();
        category.setName(data.name);
        category.setDescription(data.description);
        category.setThumbnailUrl(data.thumbnailUrl);
        if (parentId != null) {
            Optional<Category> parentCategory = categoryRepository.findById(parentId);
            parentCategory.ifPresent(category::setParent);
        }
        category.setLevel(data.level);
        category.setPath(data.path);
        category.setSortOrder(data.sortOrder);
        category.setActive(true);

        // Create SEO metadata
        Category.SeoMetadata seoMetadata = new Category.SeoMetadata();
        seoMetadata.setSlug(data.name.toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9-]", ""));
        seoMetadata.setMetaTitle(data.name + " - OCommerce");
        seoMetadata.setMetaDescription("Shop " + data.name.toLowerCase() + " at OCommerce. " + data.description);
        seoMetadata.setMetaKeywords(Arrays.asList(data.name.split(" ")));
        category.setSeoMetadata(seoMetadata);

        category = categoryRepository.save(category);
        return category.getId();
    }

    private void seedProducts() {
        logger.info("Seeding product data...");
        try {
            List<ProductSeedData> productsData = readJsonFile("seed-data/products.json", new TypeReference<List<ProductSeedData>>() {});

            for (ProductSeedData productData : productsData) {
                Product product = new Product();
                product.setName(productData.name);
                product.setShortDescription(productData.shortDescription);
                product.setLongDescription(productData.longDescription);
                product.setBasePrice(productData.basePrice);
                product.setUnitOfMeasure(productData.unitOfMeasure);
                product.setThumbnailUrl(productData.thumbnailUrl);
                product.setImageUrls(productData.imageUrls);
                product.setStatus(ProductStatus.ACTIVE);
                product.setInventoryTracking(true);
                product.setWeight(productData.weight);

                // Set categories
                if (productData.categoryNames != null) {
                    List<Category> categories = new ArrayList<>();
                    for (String categoryName : productData.categoryNames) {
                        List<Category> categoriesTemp = categoryRepository.findByNameContainingIgnoreCaseAndActive(categoryName);
                        if (categoriesTemp != null && !categoriesTemp.isEmpty()) {
                            categories.addAll(categoriesTemp);
                        }
                    }
                    product.setCategories(categories);
                }

                // Set dimensions
                if (productData.dimensions != null) {
                    Product.ProductDimensions dimensions = new Product.ProductDimensions(
                        productData.dimensions.length,
                        productData.dimensions.width,
                        productData.dimensions.height,
                        productData.dimensions.unit
                    );
                    product.setDimensions(dimensions);
                }

                // Set attributes
                if (productData.attributes != null) {
                    product.setAttributes(productData.attributes);
                }

                // Create SEO metadata
                Product.SeoMetadata seoMetadata = new Product.SeoMetadata();
                seoMetadata.setSlug(productData.name.toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9-]", ""));
                seoMetadata.setMetaTitle(productData.name + " - OCommerce");
                seoMetadata.setMetaDescription(productData.shortDescription);
                seoMetadata.setMetaKeywords(Arrays.asList(productData.name.split(" ")));
                product.setSeoMetadata(seoMetadata);

                productRepository.save(product);
            }

            logger.info("Created {} products", productRepository.count());
        } catch (IOException e) {
            logger.error("Error seeding products: ", e);
        }
    }

    private void seedProductVariants() {
        logger.info("Seeding product variants...");
        try {
            List<VariantSeedData> variantsData = readJsonFile("seed-data/variants.json", new TypeReference<List<VariantSeedData>>() {});

            // Group variants by product name
            Map<String, List<VariantSeedData>> variantsByProduct = new HashMap<>();
            for (VariantSeedData variant : variantsData) {
                variantsByProduct.computeIfAbsent(variant.productName, k -> new ArrayList<>()).add(variant);
            }

            // Get all products
            List<Product> products = productRepository.findAll();

            for (Product product : products) {
                List<VariantSeedData> productVariants = variantsByProduct.get(product.getName());
                if (productVariants != null) {
                    List<ProductVariant> variants = new ArrayList<>();

                    for (VariantSeedData variantData : productVariants) {
                        ProductVariant variant = new ProductVariant();
                        variant.setSku(variantData.sku);
                        variant.setVariantName(variantData.variantName);
                        variant.setPrice(variantData.price);
                        variant.setCompareAtPrice(variantData.compareAtPrice != null ? BigDecimal.valueOf(variantData.compareAtPrice) : null);
                        variant.setCostPrice(variantData.costPrice != null ? BigDecimal.valueOf(variantData.costPrice) : null);
                        variant.setPosition(variantData.position);
                        variant.setAttributes(variantData.attributes);
                        variant.setImageUrls(variantData.imageUrls);
                        variant.setWeight(variantData.weight);

                        // Set dimensions
                        if (variantData.dimensions != null) {
                            Product.ProductDimensions dimensions = new Product.ProductDimensions(
                                variantData.dimensions.length,
                                variantData.dimensions.width,
                                variantData.dimensions.height,
                                variantData.dimensions.unit
                            );
                            variant.setDimensions(dimensions);
                        }

                        // Set inventory
                        ProductVariant.VariantInventory inventory = new ProductVariant.VariantInventory();
                        inventory.setQuantity(variantData.inventory.quantity);
                        inventory.setLowStockThreshold(variantData.inventory.lowStockThreshold);
                        inventory.setTrackInventory(variantData.inventory.trackInventory);
                        inventory.setAllowBackorder(variantData.inventory.allowBackorder);
                        variant.setInventory(inventory);

                        variants.add(variant);
                    }

                    product.setVariants(variants);
                    productRepository.save(product);
                }
            }

            logger.info("Added variants to products");
        } catch (IOException e) {
            logger.error("Error seeding variants: ", e);
        }
    }

    private <T> T readJsonFile(String fileName, TypeReference<T> typeReference) throws IOException {
        InputStream inputStream = new ClassPathResource(fileName).getInputStream();
        return objectMapper.readValue(inputStream, typeReference);
    }

    // Data classes for JSON mapping
    public static class UserSeedData {
        public String firstName;
        public String lastName;
        public String email;
        public String password;
        public String phoneNumber;
        public boolean emailVerified;
        public boolean accountLocked;
        public List<AddressSeedData> addresses;
    }

    public static class AddressSeedData {
        public String type;
        public String streetAddress;
        public String addressLine2;
        public String city;
        public String state;
        public String postalCode;
        public String country;
        public boolean isDefault;
    }

    public static class CategorySeedData {
        public String name;
        public String description;
        public String thumbnailUrl;
        public String parentName;
        public int level;
        public String path;
        public int sortOrder;
    }

    public static class ProductSeedData {
        public String name;
        public String shortDescription;
        public String longDescription;
        public Double basePrice;
        public String unitOfMeasure;
        public String thumbnailUrl;
        public List<String> imageUrls;
        public List<String> categoryNames;
        public Double weight;
        public DimensionsSeedData dimensions;
        public Map<String, Object> attributes;
    }

    public static class VariantSeedData {
        public String productName;
        public String sku;
        public String variantName;
        public Double price;
        public Double compareAtPrice;
        public Double costPrice;
        public int position;
        public Map<String, String> attributes;
        public List<String> imageUrls;
        public Double weight;
        public DimensionsSeedData dimensions;
        public InventorySeedData inventory;
    }

    public static class DimensionsSeedData {
        public Double length;
        public Double width;
        public Double height;
        public String unit;
    }

    public static class InventorySeedData {
        public int quantity;
        public int lowStockThreshold;
        public boolean trackInventory;
        public boolean allowBackorder;
    }
}