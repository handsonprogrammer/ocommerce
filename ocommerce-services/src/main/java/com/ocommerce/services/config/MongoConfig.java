package com.ocommerce.services.config;

/**
 * MongoDB configuration that is only enabled when MongoDB is available
 * and not explicitly excluded. Disabled in test profile.
 */
/*@Configuration
@ConditionalOnClass(MongoTemplate.class)
@ConditionalOnProperty(name = "spring.data.mongodb.uri")
@Profile("!test")
@EnableMongoAuditing*/
public class MongoConfig {
}
