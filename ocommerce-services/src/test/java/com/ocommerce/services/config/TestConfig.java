package com.ocommerce.services.config;

import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Test configuration that excludes MongoDB auto-configuration.
 * This prevents Spring from trying to connect to MongoDB during tests
 * when MongoDB is not available.
 */
@Configuration
public class TestConfig {
    @Bean
    public IFeatureAwareVersion embeddedMongoVersion() {
        return Version.Main.V7_0; // Or any other desired version
    }
}
