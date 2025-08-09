# O-Commerce Services API

A comprehensive, modular monolith e-commerce API built with Spring Boot, providing robust functionality for online commerce operations with dual database support.

## 🏗️ Architecture

This project follows a **modular monolith** architecture pattern with clear domain separation:

- **User Domain**: User management, authentication, profiles (PostgreSQL)
- **Product Catalog Domain**: Products, categories, inventory (MongoDB)
- **Shopping Cart Domain**: Cart management, cart items (PostgreSQL)
- **Order Domain**: Order processing, order history (PostgreSQL)
- **Payment Domain**: Payment processing, transactions (PostgreSQL)
- **Address Domain**: Address management for users (PostgreSQL)
- **Notification Domain**: Email, SMS notifications (PostgreSQL)
- **Audit Domain**: System audit logs, user activity (PostgreSQL)
- **Analytics Domain**: Business analytics, reporting (MongoDB)

## 🚀 Technology Stack

### Core Framework

- **Spring Boot 3.2+** with Java 21
- **Spring Security** with JWT authentication
- **Spring Data JPA** for PostgreSQL
- **Spring Data MongoDB** for MongoDB
- **Spring Boot Actuator** for monitoring

### Databases

- **PostgreSQL 15** - Primary database for transactional data
- **MongoDB 7.0** - Document database for catalog and analytics

### Documentation & Testing

- **OpenAPI 3** with Swagger UI
- **JUnit 5** with **Testcontainers**
- **MapStruct** for DTO mapping

### Security & Monitoring

- **JWT** with access/refresh tokens
- **Structured logging** with Logback
- **Health checks** and metrics

## 🛠️ Getting Started

### Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **Docker & Docker Compose** (for databases)

### 1. Clone and Navigate

```bash
cd ocommerce-services
```

### 2. Start Development Databases

```bash
cd docker
docker-compose up -d

# To include admin tools (pgAdmin, Mongo Express)
docker-compose --profile admin up -d
```

### 3. Run the Application

```bash
# Development mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or compile and run
mvn clean compile
mvn spring-boot:run
```

### 4. Verify Installation

- **API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **Health Check**: http://localhost:8080/api/v1/actuator/health

## 🗄️ Database Setup

### PostgreSQL (Primary Database)

- **Host**: localhost:5432
- **Database**: ocommerce_dev
- **User**: ocommerce_user
- **Password**: ocommerce_pass

### MongoDB (Catalog Database)

- **Host**: localhost:27017
- **Database**: ocommerce_catalog_dev

### Admin Tools (Optional)

- **pgAdmin**: http://localhost:8080 (admin@ocommerce.com / admin123)
- **Mongo Express**: http://localhost:8081

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report

# Integration tests only
mvn failsafe:integration-test
```

## 📁 Project Structure

```
src/
├── main/java/com/ocommerce/services/
│   ├── OCommerceServicesApplication.java    # Main application class
│   ├── config/                              # Configuration classes
│   ├── common/                              # Shared utilities and base classes
│   ├── user/                                # User domain
│   ├── catalog/                             # Product catalog domain (MongoDB)
│   ├── cart/                                # Shopping cart domain
│   ├── order/                               # Order management domain
│   ├── payment/                             # Payment processing domain
│   ├── address/                             # Address management domain
│   ├── notification/                        # Notification domain
│   ├── audit/                               # Audit logging domain
│   └── analytics/                           # Analytics domain (MongoDB)
├── main/resources/
│   ├── application.properties               # Main configuration
│   ├── application-dev.properties           # Development profile
│   └── application-test.properties          # Test profile
└── test/                                    # Test classes
```

## 🔧 Configuration

### Profiles

- **default**: Production settings
- **dev**: Development with debug logging
- **test**: Test environment with H2/embedded databases

### Key Properties

```properties
# Application
server.port=8080
server.servlet.context-path=/api/v1

# Security
app.security.jwt.secret=${JWT_SECRET:your-secret}
app.security.jwt.access-token-expiration-ms=900000

# Databases
spring.datasource.url=jdbc:postgresql://localhost:5432/ocommerce_dev
spring.data.mongodb.uri=mongodb://localhost:27017/ocommerce_catalog_dev
```

## 🔒 Security

- **JWT Authentication** with access and refresh tokens
- **Role-based authorization** (USER, ADMIN, MODERATOR)
- **Password encryption** with BCrypt
- **CORS configuration** for web applications
- **Security headers** and CSRF protection

## 📚 API Documentation

Once running, visit:

- **Interactive API**: http://localhost:8080/api/v1/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api/v1/api-docs

## 🎯 Development Phases

### Phase 1: Foundation ✅

- [x] Project setup and configuration
- [x] Base entities and common utilities
- [x] Database configurations
- [x] Security foundation

### Phase 2: Core Domains (In Progress)

- [ ] User management and authentication
- [ ] Product catalog with MongoDB
- [ ] Basic CRUD operations

### Phase 3: E-commerce Features

- [ ] Shopping cart functionality
- [ ] Order management
- [ ] Payment integration

### Phase 4: Advanced Features

- [ ] Notifications system
- [ ] Analytics and reporting
- [ ] Performance optimization

## 🤝 Contributing

1. Follow the existing code structure
2. Write comprehensive tests
3. Update documentation
4. Follow Java coding conventions

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

For detailed requirements and specifications, see [PROJECT_REQUIREMENTS.md](../PROJECT_REQUIREMENTS.md).
