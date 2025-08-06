package com.ocommerce.services.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoDB configuration that is only enabled when MongoDB is available
 * and not explicitly excluded. Disabled in test profile.
 */
@Configuration
@ConditionalOnClass(MongoTemplate.class)
@ConditionalOnProperty(name = "spring.data.mongodb.uri")
@Profile("!test")
@EnableMongoAuditing
public class MongoConfig {
}
