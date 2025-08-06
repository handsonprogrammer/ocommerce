package com.ocommerce.services.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoDB configuration that is only enabled when MongoDB is available
 * and not explicitly excluded.
 */
@Configuration
@ConditionalOnClass(MongoTemplate.class)
@ConditionalOnProperty(name = "spring.data.mongodb.uri")
@EnableMongoAuditing
public class MongoConfig {
}
