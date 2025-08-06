package com.ocommerce.services;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test for the Spring Boot application context.
 * Uses the test profile which has no MongoDB URI configured,
 * so MongoDB configuration is disabled via @ConditionalOnProperty.
 */
@SpringBootTest
@ActiveProfiles("test")
class OCommerceServicesApplicationTests {

    @Test
    void contextLoads() {
        // This test will verify that the Spring context loads successfully
    }
}
