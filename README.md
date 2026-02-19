# Commerce-Security 🔐

A secure e-commerce platform built with Spring Boot 3.x and Spring Security, featuring JWT authentication, OAuth2 integration, and Role-Based Access Control (RBAC). This project demonstrates enterprise-level security implementation for REST and GraphQL APIs.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![JWT](https://img.shields.io/badge/JWT-Authentication-orange)

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Security Implementation](#-security-implementation)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Project Structure](#-project-structure)

## 🎯 Project Overview

This project emphasizes building secure, scalable, and production-ready web backends that can safely handle authentication, authorization, and cross-origin requests. It implements Spring Security concepts to secure REST and GraphQL APIs using JWT-based authentication, OAuth2 login (Google), and Role-Based Access Control (RBAC).

**Complexity**: Advanced | **Time Estimate**: 10-12 hours

### Learning Objectives

By completing this project, you will:

1. Apply Spring Security configurations to enforce authentication, authorization, and access control across REST and GraphQL APIs
2. Implement JWT authentication, Google OAuth2 login, and secure password hashing using BCrypt
3. Configure and analyze CORS and CSRF policies for different client interactions
4. Apply DSA concepts (hashing, encryption, and token validation) to strengthen data security
5. Develop and test role-based access control (RBAC) for real-world deployment

## ✨ Features

### Security Features
- 🔐 **JWT Authentication** - Stateless token-based authentication with secure token generation and validation
- 🌐 **OAuth2 Integration** - Google login with automatic user registration and role assignment
- 👮 **Role-Based Access Control (RBAC)** - Fine-grained permissions (ADMIN, CUSTOMER, STAFF)
- 🔒 **Password Security** - BCrypt password hashing with salt
- 🌍 **CORS Configuration** - Secure cross-origin resource sharing for web clients
- 🛡️ **CSRF Protection** - Configurable CSRF protection for stateful/stateless APIs
- 📝 **Security Event Logging** - Authentication attempts, access patterns, and security events
- 🚫 **Token Blacklisting** - Revoked token management using in-memory cache

### API Features
- 🚀 **RESTful API** - Secured REST endpoints with JWT validation
- 🎯 **GraphQL API** - Protected GraphQL queries and mutations
- 📚 **OpenAPI Documentation** - Interactive API documentation with security schemes
- ✅ **Input Validation** - Request validation and constraint checking
- 🛡️ **Error Handling** - Centralized exception handling with security context

### Business Features
- 🛍️ **Product Management** - CRUD operations with role-based restrictions
- 📂 **Category Management** - Organize products into categories
- 🛒 **Shopping Cart** - User-specific cart management
- 📦 **Order Processing** - Secure order creation and tracking
- 👥 **User Management** - User registration, profile management, and authentication

## 🏗️ Architecture

```
┌─────────────────────────────────────┐
│         Client (Browser/Postman)    │
└──────────────┬──────────────────────┘
               │ HTTPS + JWT Token
┌──────────────┴──────────────────────┐
│       Spring Security Filter Chain  │
│  ├─ CORS Filter                     │
│  ├─ JWT Authentication Filter       │
│  ├─ OAuth2 Login Filter             │
│  └─ Authorization Filter            │
└──────────────┬──────────────────────┘
               │
┌──────────────┴──────────────────────┐
│       Spring Boot Backend           │
├─────────────────────────────────────┤
│  Controllers (REST & GraphQL)       │
│  ├─ Services (Business Logic)       │
│  ├─ Security Services (Auth/JWT)    │
│  ├─ Repositories (Data Access)      │
│  └─ Entities (User, Role, Product)  │
└──────────────┬──────────────────────┘
               │ JPA/Hibernate
┌──────────────┴──────────────────────┐
│       PostgreSQL Database           │
└─────────────────────────────────────┘
```

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Security**: Spring Security 6.x
- **Authentication**: JWT (JSON Web Tokens)
- **OAuth2**: Google OAuth2 Client
- **Database**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Password Encryption**: BCryptPasswordEncoder
- **API Styles**: REST + GraphQL
- **Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven

### Security Libraries
- **JWT**: io.jsonwebtoken (JJWT)
- **OAuth2**: spring-boot-starter-oauth2-client
- **Validation**: spring-boot-starter-validation
- **Lombok**: Code generation and boilerplate reduction

## 📦 Prerequisites

- **Java Development Kit (JDK)** 21 or higher
- **Maven** 3.6+ (or use the included Maven wrapper)
- **PostgreSQL** 12+
- **Google Cloud Console Account** (for OAuth2 setup)
- **Postman** or similar API testing tool
- **Git**

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/basit-devBE/Commerce-Security.git
cd Commerce-Security
```

### 2. Database Setup

```bash
# Create a new database
createdb commerce_security_db

# Or using psql
psql -U postgres
CREATE DATABASE commerce_security_db;
\q
```

### 3. Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Create OAuth2 credentials (OAuth 2.0 Client ID)
5. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
6. Copy Client ID and Client Secret

### 4. Configure Environment Variables

Create `application-dev.properties` in `src/main/resources/`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/commerce_security_db
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password

# JWT Configuration
jwt.secret=your-256-bit-secret-key-here-make-it-long-and-secure
jwt.expiration=86400000

# OAuth2 Google Configuration
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
spring.security.oauth2.client.registration.google.scope=profile,email
```

### 5. Install Dependencies

```bash
./mvnw clean install
```

## ⚙️ Configuration

### Security Configuration

The application uses a custom `SecurityFilterChain` with the following configurations:

- **Public Endpoints**: `/auth/**`, `/oauth2/**`, `/login/**`
- **Protected Endpoints**: `/api/**`, `/graphql/**`
- **Admin Endpoints**: `/api/admin/**`, `/api/products/**` (POST, PUT, DELETE)
- **CORS**: Configured for `http://localhost:3000` and other specified origins
- **CSRF**: Disabled for stateless JWT APIs
- **Session Management**: Stateless (no server-side sessions)

### Application Properties

```properties
# Application Name
spring.application.name=Commerce-Security

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Security Logging
logging.level.org.springframework.security=DEBUG
```

## 🔒 Security Implementation

### 1. JWT Authentication

**Token Generation**:
```java
// Login endpoint generates JWT with claims
POST /auth/login
{
  "username": "user@example.com",
  "password": "password123"
}

// Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

**Token Structure**:
- **Header**: Algorithm (HS256) and token type
- **Payload**: Subject (username), roles, issued time, expiration
- **Signature**: HMAC SHA-256 signature for verification

**Token Validation**:
- Signature verification using secret key
- Expiration check
- Claims extraction and validation
- Automatic rejection of tampered or expired tokens

### 2. OAuth2 (Google Login)

```bash
# Initiate Google login
GET /oauth2/authorization/google

# Callback endpoint (automatic)
GET /login/oauth2/code/google
```

**Flow**:
1. User clicks "Login with Google"
2. Redirected to Google consent screen
3. Google returns authorization code
4. Application exchanges code for access token
5. Fetches user profile from Google
6. Creates or updates user in database
7. Assigns default role (CUSTOMER)
8. Generates JWT token for subsequent requests

### 3. Role-Based Access Control (RBAC)

**Roles**:
- `ADMIN`: Full access to all endpoints
- `CUSTOMER`: Access to shopping features (cart, orders, profile)
- `STAFF`: Access to inventory and order management

**Implementation**:
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/api/products")
public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request) {
    // Only admins can create products
}

@PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
@GetMapping("/api/orders")
public ResponseEntity<List<Order>> getOrders() {
    // Admins and customers can view orders
}
```

### 4. Password Security

- **Hashing Algorithm**: BCrypt with automatic salt generation
- **Strength**: 10 rounds (configurable)
- **Storage**: Only hashed passwords stored in database
- **Validation**: Automatic comparison during login

### 5. CORS Configuration

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    return source;
}
```

**CORS vs CSRF**:
- **CORS**: Controls which origins can access your API (browser security)
- **CSRF**: Prevents unauthorized commands from trusted users (form submissions)
- **JWT APIs**: CSRF disabled (stateless), CORS enabled for cross-origin access

### 6. Security Event Logging

All authentication and authorization events are logged:
- Login attempts (success/failure)
- Token generation and validation
- Access denied events
- OAuth2 authentication flow
- Role-based access violations

### 7. Token Blacklisting (DSA Implementation)

Uses in-memory HashMap for revoked token management:
```java
// Logout endpoint adds token to blacklist
POST /auth/logout
Authorization: Bearer <token>

// Token validation checks blacklist
if (tokenBlacklist.contains(token)) {
    throw new UnauthorizedException("Token has been revoked");
}
```

## 📚 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "SecurePass123!"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "user@example.com",
  "roles": ["CUSTOMER"]
}
```

#### Google OAuth2 Login
```http
GET /oauth2/authorization/google
```

#### Logout
```http
POST /auth/logout
Authorization: Bearer <token>
```

### Protected Endpoints

All protected endpoints require JWT token in Authorization header:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Products (Public Read, Admin Write)
```http
GET /api/products                    # Public
GET /api/products/{id}               # Public
POST /api/products                   # Admin only
PUT /api/products/{id}               # Admin only
DELETE /api/products/{id}            # Admin only
```

#### Orders (Customer & Admin)
```http
GET /api/orders                      # Customer (own orders) / Admin (all)
GET /api/orders/{id}                 # Customer (own) / Admin (all)
POST /api/orders                     # Customer
PUT /api/orders/{id}/status          # Admin only
```

#### Cart (Customer)
```http
GET /api/cart                        # Customer
POST /api/cart/items                 # Customer
PUT /api/cart/items/{id}             # Customer
DELETE /api/cart/items/{id}          # Customer
```

#### Admin Endpoints
```http
GET /api/admin/users                 # Admin only
GET /api/admin/metrics               # Admin only
PUT /api/admin/users/{id}/role       # Admin only
```

### GraphQL API

Access GraphQL Playground at `http://localhost:8080/graphiql`

**Authentication**: Include JWT token in HTTP headers:
```json
{
  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Example Queries**:
```graphql
# Get all products (public)
query {
  products {
    id
    name
    price
    description
  }
}

# Create product (admin only)
mutation {
  createProduct(input: {
    name: "New Product"
    price: 29.99
    description: "Product description"
    categoryId: 1
  }) {
    id
    name
  }
}
```

## 🧪 Testing

### Postman Testing

1. **Import Collection**: Import the provided Postman collection
2. **Set Environment Variables**:
   - `baseUrl`: `http://localhost:8080`
   - `token`: (will be set automatically after login)

3. **Test Scenarios**:

**Scenario 1: User Registration and Login**
```
1. POST /auth/register (create new user)
2. POST /auth/login (get JWT token)
3. Verify token is returned and valid
```

**Scenario 2: JWT Token Validation**
```
1. POST /auth/login (get token)
2. GET /api/products (with token - should succeed)
3. GET /api/products (without token - should fail with 401)
4. GET /api/products (with expired token - should fail with 401)
5. GET /api/products (with tampered token - should fail with 401)
```

**Scenario 3: Role-Based Access Control**
```
1. Login as CUSTOMER
2. GET /api/products (should succeed)
3. POST /api/products (should fail with 403 Forbidden)
4. Login as ADMIN
5. POST /api/products (should succeed)
```

**Scenario 4: OAuth2 Google Login**
```
1. GET /oauth2/authorization/google (browser)
2. Complete Google authentication
3. Verify user created in database
4. Verify JWT token returned
```

**Scenario 5: CORS Testing**
```
1. Send request from allowed origin (should succeed)
2. Send request from unauthorized origin (should fail)
3. Verify preflight OPTIONS requests handled correctly
```

### Unit Tests

```bash
# Run all tests
./mvnw test

# Run security tests only
./mvnw test -Dtest=SecurityConfigTest

# Run with coverage
./mvnw test jacoco:report
```

### Integration Tests

```bash
# Run integration tests
./mvnw verify
```

## 📁 Project Structure

```
Commerce-Security/
├── src/
│   ├── main/
│   │   ├── java/com/example/commerce/
│   │   │   ├── CommerceApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java          # Spring Security configuration
│   │   │   │   ├── JwtConfig.java               # JWT configuration
│   │   │   │   ├── CorsConfig.java              # CORS configuration
│   │   │   │   └── OAuth2Config.java            # OAuth2 configuration
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java # JWT filter
│   │   │   │   ├── JwtTokenProvider.java        # JWT generation/validation
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   └── OAuth2SuccessHandler.java    # OAuth2 success handler
│   │   │   ├── controllers/
│   │   │   │   ├── AuthController.java          # Authentication endpoints
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   └── AdminController.java
│   │   │   ├── entities/
│   │   │   │   ├── User.java                    # User entity with roles
│   │   │   │   ├── Role.java                    # Role entity
│   │   │   │   ├── Product.java
│   │   │   │   └── Order.java
│   │   │   ├── repositories/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   └── ...
│   │   │   ├── services/
│   │   │   │   ├── AuthService.java             # Authentication service
│   │   │   │   ├── UserService.java
│   │   │   │   └── ...
│   │   │   ├── dtos/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   └── ...
│   │   │   └── exceptions/
│   │   │       ├── UnauthorizedException.java
│   │   │       ├── ForbiddenException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── graphql/
│   │           └── schema.graphqls
│   └── test/
│       └── java/com/example/commerce/
│           ├── SecurityConfigTest.java
│           ├── JwtTokenProviderTest.java
│           └── AuthControllerTest.java
├── pom.xml
└── README.md
```

## 🏃 Running the Application

### Start the Application

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using Maven
mvn spring-boot:run

# With specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

### Access Points

- **API Base URL**: http://localhost:8080/api
- **GraphQL Playground**: http://localhost:8080/graphiql
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

### Default Users

After running the application, default users are seeded:

| Username | Password | Role |
|----------|----------|------|
| admin@commerce.com | admin123 | ADMIN |
| customer@commerce.com | customer123 | CUSTOMER |
| staff@commerce.com | staff123 | STAFF |

## 🔍 DSA and Security Optimization

### 1. Hashing (Password Security)
- **Algorithm**: BCrypt with salt
- **Complexity**: O(1) for hash generation and verification
- **Security**: Resistant to rainbow table attacks

### 2. Token Validation (HashMap Lookup)
- **Data Structure**: HashMap for blacklisted tokens
- **Complexity**: O(1) average case for lookup
- **Use Case**: Fast token revocation checking

### 3. Role Lookup (Set Operations)
- **Data Structure**: Set for user roles
- **Complexity**: O(1) for role membership check
- **Use Case**: Quick authorization decisions

### 4. Caching Strategy
- **Implementation**: In-memory cache for frequently accessed data
- **Benefit**: Reduced database queries for user details
- **Eviction**: Time-based expiration

## 🐛 Troubleshooting

### Common Issues

**1. JWT Token Invalid**
- Verify secret key matches in configuration
- Check token expiration time
- Ensure token format: `Bearer <token>`

**2. OAuth2 Login Fails**
- Verify Google Client ID and Secret
- Check redirect URI matches Google Console
- Ensure Google+ API is enabled

**3. 403 Forbidden Error**
- Verify user has required role
- Check @PreAuthorize annotations
- Review security filter chain configuration

**4. CORS Error in Browser**
- Add origin to allowed origins list
- Verify CORS configuration
- Check preflight OPTIONS requests

**5. Database Connection Error**
- Verify PostgreSQL is running
- Check database credentials
- Ensure database exists

## 📊 Evaluation Criteria

| Category | Points |
|----------|--------|
| Security Configuration (CORS & CSRF) | 15 |
| JWT Implementation | 20 |
| OAuth2 (Google Integration) | 15 |
| RBAC and Role Enforcement | 15 |
| DSA in Security | 15 |
| Testing & Logging | 10 |
| Code Quality & Documentation | 10 |
| **Total** | **100** |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/SecurityFeature`)
3. Commit your changes (`git commit -m 'Add security feature'`)
4. Push to the branch (`git push origin feature/SecurityFeature`)
5. Open a Pull Request

## 📄 License

This project is available for educational purposes.

## 👨💻 Author

**basit-devBE** - [GitHub Profile](https://github.com/basit-devBE)

## 🙏 Acknowledgments

- Spring Security team for comprehensive security framework
- JWT.io for token standards and tools
- Google OAuth2 for authentication integration
- PostgreSQL community for robust database system

---

**Secure Coding! 🔐**
