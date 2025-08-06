package com.ocommerce.services.config;

import com.ocommerce.services.config.ApplicationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Main configuration class that enables configuration properties.
 */
@Configuration
@EnableConfigurationProperties({
        ApplicationProperties.class
})
public class AppConfig {
}
