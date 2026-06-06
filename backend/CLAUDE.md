# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build all modules
mvn clean install

# Build a specific module
mvn clean install -pl api-gateway

# Run a service (after build)
mvn spring-boot:run -pl api-gateway

# Compile only (no tests)
mvn compile -DskipTests
```

## Architecture Overview

Multi-module Maven project for a trading platform using microservices architecture with Java 21 and Spring Boot 3.3.4.

### Modules

| Module | Port | Description |
|--------|------|-------------|
| `api-gateway` | 8090 | Spring Cloud Gateway - JWT validation, routing, CORS |
| `market-data` | 8081 | Reactive service for market data (WebFlux + R2DBC) |
| `portfolio` | 8082 | Reactive service for portfolio management |
| `user-manager` | N/A | Domain module with DDD entities (no Spring Boot app) |

### Key Technologies

- **Reactive Stack**: WebFlux, R2DBC (reactive PostgreSQL driver)
- **Security**: OAuth2 Resource Server with JWT validation against Keycloak
- **Database**: PostgreSQL with Flyway migrations
- **Messaging**: Spring Kafka
- **Code Generation**: Lombok + MapStruct (annotation processors configured in parent POM)
- **DDD**: jMolecules annotations in `user-manager` module

### API Gateway Routing

The gateway routes requests by stripping the `/api` prefix:
- `/api/market/**` → `localhost:8081` (market-data service)
- `/api/portfolio/**` → `localhost:8082` (portfolio service)

### Authentication Flow

1. Keycloak runs on port 8080 (start with `docker compose up -d`)
2. Gateway validates JWT tokens using Keycloak's public key (JWKS)
3. Keycloak realm: `trading-app`, issuer: `http://localhost:8080/realms/trading-app`
4. Public key is cached on startup - no per-request call to Keycloak

### DDD in user-manager

The `user-manager` module uses jMolecules DDD annotations:
- `@AggregateRoot` marks aggregate roots
- Value objects are modeled as Java records (e.g., `UserID`)
- Package structure: `domain.model` for domain entities

## Running the Application

```bash
# Start Keycloak and its PostgreSQL database
docker compose up -d

# Start the API Gateway
mvn spring-boot:run -pl api-gateway
```

Keycloak admin console: http://localhost:8080 (admin/admin)

## Testing Requests

When testing authenticated endpoints, obtain a token from Keycloak first:
```http
POST http://localhost:8080/realms/trading-app/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=account&username=<user>&password=<pass>
```