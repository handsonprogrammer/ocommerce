package com.ocommerce.services.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

@Configuration
@SecurityScheme(name = "Bearer Authentication", // A unique name for your security scheme
        type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SwaggerConfig {

}
