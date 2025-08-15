package com.ocommerce.services.seed;

import com.ocommerce.services.catalog.constants.ProductStatus;
import com.ocommerce.services.catalog.domain.Category;
import com.ocommerce.services.catalog.domain.Product;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Seed data service to populate initial data for development and testing
 * Only runs in 'dev' profile to avoid accidentally seeding production data
 */
//@Component
//@Profile("dev")
public class SeedDataServiceStandAlone implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SeedDataServiceStandAlone.class);

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

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting seed data generation...");

        if (shouldSeedData()) {
            seedUsers();
            seedCategories();
            seedProducts();
            logger.info("Seed data generation completed successfully!");
        } else {
            logger.info("Data already exists, skipping seed data generation.");
        }
    }

    /**
     * Check if we should seed data (only if databases are empty)
     */
    private boolean shouldSeedData() {
        return userRepository.count() == 0 && categoryRepository.count() == 0;
    }

    /**
     * Seed user data with sample users and addresses
     */
    private void seedUsers() {
        logger.info("Seeding user data...");

        // Create admin user
        User admin = createUser(
                "John", "Admin", "admin@ocommerce.com", "Admin123!",
                "+1-555-0001", true, false
        );
        userRepository.save(admin);

        // Create sample customers
        User customer1 = createUser(
                "Alice", "Johnson", "alice.johnson@email.com", "Password123!",
                "+1-555-0002", true, false
        );
        userRepository.save(customer1);

        User customer2 = createUser(
                "Bob", "Smith", "bob.smith@email.com", "Password123!",
                "+1-555-0003", true, false
        );
        userRepository.save(customer2);

        User customer3 = createUser(
                "Carol", "Brown", "carol.brown@email.com", "Password123!",
                "+1-555-0004", true, false
        );
        userRepository.save(customer3);

        User customer4 = createUser(
                "David", "Wilson", "david.wilson@email.com", "Password123!",
                "+1-555-0005", false, false
        );
        userRepository.save(customer4);

        // Create addresses for users
        seedAddresses(customer1, customer2, customer3);

        logger.info("Created {} users", userRepository.count());
    }

    /**
     * Create a user with the given details
     */
    private User createUser(String firstName, String lastName, String email, String password,
                           String phoneNumber, boolean emailVerified, boolean accountLocked) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhoneNumber(phoneNumber);
        user.setEmailVerified(emailVerified);
        user.setAccountLocked(accountLocked);
        return user;
    }

    /**
     * Seed addresses for users
     */
    private void seedAddresses(User... users) {
        logger.info("Seeding address data...");

        // Addresses for Alice Johnson
        addressRepository.save(createAddress(users[0], "Home", "123 Main St", "",
                "New York", "NY", "10001", "USA", true, false));
        addressRepository.save(createAddress(users[0], "Work", "456 Business Ave", "Suite 200",
                "New York", "NY", "10002", "USA", false, false));

        // Addresses for Bob Smith
        addressRepository.save(createAddress(users[1], "Home", "789 Oak Street", "",
                "Los Angeles", "CA", "90210", "USA", true, false));

        // Addresses for Carol Brown
        addressRepository.save(createAddress(users[2], "Home", "321 Pine Road", "Apt 5B",
                "Chicago", "IL", "60601", "USA", true, false));
        addressRepository.save(createAddress(users[2], "Office", "654 Corporate Blvd", "Floor 15",
                "Chicago", "IL", "60602", "USA", false, false));

        logger.info("Created {} addresses", addressRepository.count());
    }

    /**
     * Create an address with the given details
     */
    private Address createAddress(User user, String label, String street, String street2,
                                 String city, String state, String zipCode, String country,
                                 boolean isDefault, boolean isDeleted) {
        Address address = new Address();
        address.setUser(user);
        address.setType(label);
        address.setStreetAddress(street);
        address.setAddressLine2(street2);
        address.setCity(city);
        address.setState(state);
        address.setPostalCode(zipCode);
        address.setCountry(country);
        address.setDefault(isDefault);
        address.setDeleted(isDeleted);
        return address;
    }

    /**
     * Seed category hierarchy
     */
    private void seedCategories() {
        logger.info("Seeding category data...");

        Map<String, UUID> categoryIds = new HashMap<>();

        // Create root categories
        UUID electronicsId = createCategory("Electronics", "Electronic devices and gadgets",
                null, 0, "/electronics", 1, categoryIds);

        UUID clothingId = createCategory("Clothing", "Fashion and apparel",
                null, 0, "/clothing", 2, categoryIds);

        UUID homeId = createCategory("Home & Garden", "Home improvement and garden supplies",
                null, 0, "/home-garden", 3, categoryIds);

        UUID sportsId = createCategory("Sports & Outdoors", "Sports equipment and outdoor gear",
                null, 0, "/sports-outdoors", 4, categoryIds);

        // Electronics subcategories
        UUID computersId = createCategory("Computers", "Laptops, desktops, and accessories",
                electronicsId, 1, "/electronics/computers", 1, categoryIds);

        UUID mobileId = createCategory("Mobile & Tablets", "Smartphones and tablets",
                electronicsId, 1, "/electronics/mobile-tablets", 2, categoryIds);

        UUID audioId = createCategory("Audio & Video", "Headphones, speakers, and entertainment",
                electronicsId, 1, "/electronics/audio-video", 3, categoryIds);

        // Computer subcategories
        createCategory("Laptops", "Portable computers",
                computersId, 2, "/electronics/computers/laptops", 1, categoryIds);

        createCategory("Desktops", "Desktop computers",
                computersId, 2, "/electronics/computers/desktops", 2, categoryIds);

        createCategory("Accessories", "Computer accessories and peripherals",
                computersId, 2, "/electronics/computers/accessories", 3, categoryIds);

        // Clothing subcategories
        UUID menClothingId = createCategory("Men's Clothing", "Men's fashion and apparel",
                clothingId, 1, "/clothing/mens", 1, categoryIds);

        UUID womenClothingId = createCategory("Women's Clothing", "Women's fashion and apparel",
                clothingId, 1, "/clothing/womens", 2, categoryIds);

        createCategory("Kids Clothing", "Children's clothing",
                clothingId, 1, "/clothing/kids", 3, categoryIds);

        // Men's clothing subcategories
        createCategory("Shirts", "Men's shirts and tops",
                menClothingId, 2, "/clothing/mens/shirts", 1, categoryIds);

        createCategory("Pants", "Men's pants and trousers",
                menClothingId, 2, "/clothing/mens/pants", 2, categoryIds);

        // Women's clothing subcategories
        createCategory("Dresses", "Women's dresses",
                womenClothingId, 2, "/clothing/womens/dresses", 1, categoryIds);

        createCategory("Tops", "Women's shirts and blouses",
                womenClothingId, 2, "/clothing/womens/tops", 2, categoryIds);

        logger.info("Created {} categories", categoryRepository.count());
    }

    /**
     * Create a category with the given details
     */
    private UUID createCategory(String name, String description, UUID parentId, Integer level,
                               String path, Integer sortOrder, Map<String, UUID> categoryIds) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setLevel(level);
        category.setPath(path);
        category.setSortOrder(sortOrder);
        category.setActive(true);
        category.setProductCount(0L);

        // Create SEO metadata
        Category.SeoMetadata seoMetadata = new Category.SeoMetadata();
        seoMetadata.setSlug(name.toLowerCase().replace(" ", "-").replace("&", "and"));
        seoMetadata.setMetaTitle(name + " - OCommerce");
        seoMetadata.setMetaDescription("Shop " + name.toLowerCase() + " at OCommerce. " + description);
        category.setSeoMetadata(seoMetadata);

        category = categoryRepository.save(category);
        categoryIds.put(name, category.getId());

        return category.getId();
    }

    /**
     * Seed products with sample data
     */
    private void seedProducts() {
        logger.info("Seeding product data...");

        // Get category IDs for product assignment
        List<Category> categories = categoryRepository.findAll();
        Map<String, UUID> categoryMap = new HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getName(), category.getId());
        }

        // Electronics - Laptops
        createProduct("MacBook Pro 16-inch", "Apple MacBook Pro 16-inch with M2 Pro chip",
                "Professional laptop with advanced M2 Pro chip, 16GB RAM, and 512GB SSD storage. Perfect for developers and content creators.",
                2499.99, "piece",
                Arrays.asList(categoryMap.get("Electronics"), categoryMap.get("Computers"), categoryMap.get("Laptops")),
                "https://images.example.com/macbook-pro-16.jpg",
                Arrays.asList("https://images.example.com/macbook-pro-16-1.jpg", "https://images.example.com/macbook-pro-16-2.jpg"));

        createProduct("Dell XPS 13", "Dell XPS 13 Ultra-portable Laptop",
                "Compact and powerful laptop with Intel Core i7, 16GB RAM, and 1TB SSD. Ultra-portable design for professionals on the go.",
                299.99, "piece",
                Arrays.asList(categoryMap.get("Electronics"), categoryMap.get("Computers"), categoryMap.get("Laptops")),
                "https://images.example.com/dell-xps-13.jpg",
                Arrays.asList("https://images.example.com/dell-xps-13-1.jpg", "https://images.example.com/dell-xps-13-2.jpg"));

        createProduct("Gaming Desktop PC", "High-Performance Gaming Desktop",
                "Custom gaming PC with NVIDIA RTX 4070, Intel Core i7-13700K, 32GB RAM, and 1TB NVMe SSD. Ready for 4K gaming.",
                1899.99, "piece",
                Arrays.asList(categoryMap.get("Electronics"), categoryMap.get("Computers"), categoryMap.get("Desktops")),
                "https://images.example.com/gaming-desktop.jpg",
                Arrays.asList("https://images.example.com/gaming-desktop-1.jpg", "https://images.example.com/gaming-desktop-2.jpg"));

        // Electronics - Mobile & Tablets
        createProduct("iPhone 15 Pro", "Apple iPhone 15 Pro with Titanium Design",
                "Latest iPhone with A17 Pro chip, titanium design, and advanced camera system. Available in multiple colors.",
                999.99, "piece",
                Arrays.asList(categoryMap.get("Electronics"), categoryMap.get("Mobile & Tablets")),
                "https://images.example.com/iphone-15-pro.jpg",
                Arrays.asList("https://images.example.com/iphone-15-pro-1.jpg", "https://images.example.com/iphone-15-pro-2.jpg"));

        createProduct("Samsung Galaxy S24 Ultra", "Samsung Galaxy S24 Ultra Smartphone",
                "Premium Android smartphone with S Pen, 200MP camera, and 6.8-inch Dynamic AMOLED display.",
                1199.99, "piece",
                Arrays.asList(categoryMap.get("Electronics"), categoryMap.get("Mobile & Tablets")),
                "https://images.example.com/galaxy-s24-ultra.jpg",
                Arrays.asList("https://images.example.com/galaxy-s24-ultra-1.jpg", "https://images.example.com/galaxy-s24-ultra-2.jpg"));

        // Electronics - Audio & Video
        createProduct("Sony WH-1000XM5 Headphones", "Premium Noise Cancelling Headphones",
                "Industry-leading noise cancellation with exceptional sound quality. 30-hour battery life and multipoint connection.",
                399.99, "piece",
                Arrays.asList(categoryMap.get("Electronics"), categoryMap.get("Audio & Video")),
                "https://images.example.com/sony-wh1000xm5.jpg",
                Arrays.asList("https://images.example.com/sony-wh1000xm5-1.jpg"));

        // Clothing - Men's
        createProduct("Classic Cotton T-Shirt", "Premium Men's Cotton T-Shirt",
                "100% organic cotton t-shirt with comfortable fit. Available in multiple colors and sizes.",
                29.99, "piece",
                Arrays.asList(categoryMap.get("Clothing"), categoryMap.get("Men's Clothing"), categoryMap.get("Shirts")),
                "https://images.example.com/mens-tshirt.jpg",
                Arrays.asList("https://images.example.com/mens-tshirt-1.jpg", "https://images.example.com/mens-tshirt-2.jpg"));

        createProduct("Slim Fit Jeans", "Men's Slim Fit Denim Jeans",
                "Classic blue denim jeans with slim fit design. Durable construction with comfortable stretch fabric.",
                79.99, "piece",
                Arrays.asList(categoryMap.get("Clothing"), categoryMap.get("Men's Clothing"), categoryMap.get("Pants")),
                "https://images.example.com/mens-jeans.jpg",
                List.of("https://images.example.com/mens-jeans-1.jpg"));

        // Clothing - Women's
        createProduct("Summer Floral Dress", "Women's Elegant Summer Dress",
                "Beautiful floral print dress perfect for summer occasions. Lightweight fabric with comfortable fit.",
                89.99, "piece",
                Arrays.asList(categoryMap.get("Clothing"), categoryMap.get("Women's Clothing"), categoryMap.get("Dresses")),
                "https://images.example.com/womens-dress.jpg",
                Arrays.asList("https://images.example.com/womens-dress-1.jpg", "https://images.example.com/womens-dress-2.jpg"));

        createProduct("Silk Blouse", "Women's Premium Silk Blouse",
                "Elegant silk blouse suitable for both professional and casual wear. Available in multiple colors.",
                129.99, "piece",
                Arrays.asList(categoryMap.get("Clothing"), categoryMap.get("Women's Clothing"), categoryMap.get("Tops")),
                "https://images.example.com/womens-blouse.jpg",
                List.of("https://images.example.com/womens-blouse-1.jpg"));

        // Home & Garden
        createProduct("Robot Vacuum Cleaner", "Smart Robot Vacuum with Mapping",
                "Intelligent robot vacuum with room mapping, app control, and automatic charging. Perfect for busy households.",
                299.99, "piece",
                Arrays.asList(categoryMap.get("Home & Garden")),
                "https://images.example.com/robot-vacuum.jpg",
                Arrays.asList("https://images.example.com/robot-vacuum-1.jpg"));

        createProduct("Garden Tool Set", "Complete 10-Piece Garden Tool Set",
                "Professional-grade garden tools including shovel, rake, pruners, and more. Comes with storage bag.",
               149.99, "set",
                Arrays.asList(categoryMap.get("Home & Garden")),
                "https://images.example.com/garden-tools.jpg",
                Arrays.asList("https://images.example.com/garden-tools-1.jpg", "https://images.example.com/garden-tools-2.jpg"));

        // Sports & Outdoors
        createProduct("Professional Tennis Racket", "Carbon Fiber Tennis Racket",
                "Professional-grade tennis racket with carbon fiber construction. Perfect balance of power and control.",
                99.99, "piece",
                Arrays.asList(categoryMap.get("Sports & Outdoors")),
                "https://images.example.com/tennis-racket.jpg",
                List.of("https://images.example.com/tennis-racket-1.jpg"));

        createProduct("Camping Tent 4-Person", "Waterproof 4-Person Camping Tent",
                "Spacious 4-person tent with waterproof construction and easy setup. Includes rainfly and footprint.",
                179.99, "piece",
                Arrays.asList(categoryMap.get("Sports & Outdoors")),
                "https://images.example.com/camping-tent.jpg",
                Arrays.asList("https://images.example.com/camping-tent-1.jpg", "https://images.example.com/camping-tent-2.jpg"));

        logger.info("Created {} products", productRepository.count());
    }

    /**
     * Create a product with the given details
     */
    private void createProduct(String name, String shortDescription, String longDescription,
                              Double basePrice, String unitOfMeasure, List<UUID> categoryIds,
                              String thumbnailUrl, List<String> imageUrls) {
        Product product = new Product();
        product.setName(name);
        product.setShortDescription(shortDescription);
        product.setLongDescription(longDescription);
        product.setBasePrice(basePrice);
        product.setUnitOfMeasure(unitOfMeasure);
        product.setThumbnailUrl(thumbnailUrl);
        product.setImageUrls(imageUrls);
        product.setStatus(ProductStatus.ACTIVE);
        product.setInventoryTracking(true);



        // Create SEO metadata
        Product.SeoMetadata seoMetadata = new Product.SeoMetadata();
        seoMetadata.setSlug(name.toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9-]", ""));
        seoMetadata.setMetaTitle(name + " - OCommerce");
        seoMetadata.setMetaDescription(shortDescription);
        seoMetadata.setMetaKeywords(Arrays.asList(name.split(" ")));
        product.setSeoMetadata(seoMetadata);

        // Set dimensions for some products
        if (name.contains("Laptop") || name.contains("Desktop")) {
            Product.ProductDimensions dimensions = new Product.ProductDimensions();
            dimensions.setLength(35.0);
            dimensions.setWidth(25.0);
            dimensions.setHeight(2.5);
            dimensions.setUnit("cm");
            product.setDimensions(dimensions);
            product.setWeight(2.1);
        } else if (name.contains("Phone")) {
            Product.ProductDimensions dimensions = new Product.ProductDimensions();
            dimensions.setLength(15.0);
            dimensions.setWidth(7.5);
            dimensions.setHeight(0.8);
            dimensions.setUnit("cm");
            product.setDimensions(dimensions);
            product.setWeight(0.2);
        }

        // Add some sample attributes
        Map<String, Object> attributes = new HashMap<>();
        if (name.contains("MacBook") || name.contains("Dell")) {
            attributes.put("brand", name.contains("MacBook") ? "Apple" : "Dell");
            attributes.put("processor", name.contains("MacBook") ? "M2 Pro" : "Intel Core i7");
            attributes.put("ram", "16GB");
            attributes.put("storage", name.contains("MacBook") ? "512GB" : "1TB");
            attributes.put("screen_size", name.contains("MacBook") ? "16 inch" : "13.3 inch");
        } else if (name.contains("iPhone") || name.contains("Galaxy")) {
            attributes.put("brand", name.contains("iPhone") ? "Apple" : "Samsung");
            attributes.put("operating_system", name.contains("iPhone") ? "iOS" : "Android");
            attributes.put("storage_options", Arrays.asList("128GB", "256GB", "512GB", "1TB"));
            attributes.put("colors", Arrays.asList("Black", "White", "Blue", "Gold"));
        } else if (name.contains("T-Shirt") || name.contains("Jeans") || name.contains("Dress")) {
            attributes.put("material", name.contains("T-Shirt") ? "100% Cotton" :
                          name.contains("Jeans") ? "98% Cotton, 2% Elastane" : "Polyester Blend");
            attributes.put("care_instructions", "Machine wash cold, tumble dry low");
            attributes.put("sizes", Arrays.asList("XS", "S", "M", "L", "XL", "XXL"));
            if (name.contains("T-Shirt") || name.contains("Dress")) {
                attributes.put("colors", Arrays.asList("Black", "White", "Navy", "Red", "Green"));
            }
        }
        product.setAttributes(attributes);

        productRepository.save(product);
    }
}
