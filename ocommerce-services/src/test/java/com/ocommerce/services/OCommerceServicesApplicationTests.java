package com.ocommerce.services;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test for the Spring Boot application context.
 * Excludes MongoDB auto-configuration for testing.
 */
@SpringBootTest(classes = OCommerceServicesApplication.class, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
})
@ActiveProfiles("test")
class OCommerceServicesApplicationTests {

    @Test
    void contextLoads() {
        // This test will verify that the Spring context loads successfully
    }
}
