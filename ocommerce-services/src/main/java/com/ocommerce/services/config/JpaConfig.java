package com.ocommerce.services.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
        "com.ocommerce.services.user",
        "com.ocommerce.services.cart",
        "com.ocommerce.services.order",
        "com.ocommerce.services.payment"
})
public class JpaConfig {
}
