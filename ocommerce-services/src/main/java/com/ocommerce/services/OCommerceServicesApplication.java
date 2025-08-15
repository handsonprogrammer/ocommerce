package com.ocommerce.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Main application class for the O-Commerce Services API.
 * This is a modular monolith architecture that provides comprehensive
 * e-commerce functionality with support for both PostgreSQL and MongoDB.
 */
@SpringBootApplication
public class OCommerceServicesApplication {


    public static void main(String[] args) {
        SpringApplication.run(OCommerceServicesApplication.class, args);
    }
}
