# API Gateway

The API Gateway is the single entry point for all client requests in the Trading Platform. No client ever calls a microservice directly — every request passes through here first.

---

## Responsibilities

1. **JWT Validation** — verifies the token signature and expiry before routing any request
2. **Routing** — forwards validated requests to the appropriate downstream microservice
3. **Cross-cutting concerns** — CORS, rate limiting, and request logging in one place

---

## How JWT Validation Works

### What is a JWT?

A JSON Web Token (JWT) has three parts separated by dots:

```
header.payload.signature
```

- **Header** — the signing algorithm used (RS256)
- **Payload** — the claims: user data in plain-text JSON
- **Signature** — a digital signature that guarantees the payload has not been tampered with

### Key Claims

```json
{
  "sub": "user-uuid-123",
  "preferred_username": "normal_user",
  "realm_access": {
    "roles": ["ROLE_USER"]
  },
  "exp": 1716239022,
  "iss": "http://localhost:8080/realms/trading-app"
}
```

| Claim | Description |
|---|---|
| `sub` | Unique user identifier |
| `preferred_username` | Username |
| `realm_access.roles` | Roles assigned in Keycloak |
| `exp` | Expiry timestamp |
| `iss` | Token issuer (must match Keycloak realm URL) |

### Why is the payload safe if it is not encrypted?

The payload travels as base64-encoded plain text, but it **cannot be modified**. If anyone intercepts the token and changes `ROLE_USER` to `ROLE_ADMIN`, the signature becomes invalid and the Gateway rejects the request with `401 Unauthorized`. Without Keycloak's private key it is impossible to generate a valid signature.

---

## The Full Authentication Flow

```
1. Client        →  Keycloak      POST /token  (username + password)
2. Keycloak      →  Client        signed JWT (using Keycloak's private key)
3. Client        →  API Gateway   request + Authorization: Bearer <token>
4. API Gateway   →  Keycloak      download public key once on startup (JWKS endpoint)
5. API Gateway               verifies JWT signature locally — no Keycloak call per request
6. API Gateway               checks exp (not expired) and roles
7. API Gateway   →  Service       routes the request if everything is valid
```

> **Key point**: Keycloak is only called once at startup to download the public key.
> After that, every JWT is verified locally — no round-trip to Keycloak per request.

---

## Keycloak Integration

The Gateway connects to Keycloak via the `issuer-uri` property. On startup, Spring automatically fetches the public key from:

```
http://localhost:8080/realms/trading-app/.well-known/openid-configuration
```

This is configured in `application.yml`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/trading-app
```

---

## Routing Configuration

Each route defines:
- **id** — a unique name for the route
- **uri** — the address of the downstream microservice
- **predicates** — the URL pattern that triggers this route
- **filters** — transformations applied before forwarding (`StripPrefix=1` removes the `/api` prefix)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: market-data-route
          uri: http://localhost:8081
          predicates:
            - Path=/api/market/**
          filters:
            - StripPrefix=1

        - id: portfolio-route
          uri: http://localhost:8082
          predicates:
            - Path=/api/portfolio/**
          filters:
            - StripPrefix=1
```

Example: a request to `/api/market/prices` is forwarded to `http://localhost:8081/prices`.

## Running the Gateway

Make sure Keycloak is running first:

```bash
docker compose up -d
```

Then start the Gateway. It will be available at:

```
http://localhost:8090
```

---

## Testing with IntelliJ HTTP Client

Create a file `gateway-test.http` at the project root:

```http
### Step 1: obtain a token from Keycloak
POST http://localhost:8080/realms/trading-app/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=account&username=normal_user&password=user1234

### Step 2: call the gateway with the token (replace <access_token> with the value from step 1)
GET http://localhost:8090/api/market/prices
Authorization: Bearer <access_token>
```

---

## Port Reference

| Service             | Port |
|---------------------|------|
| Keycloak            | 8080 |
| PostgreSQL          | 5432 |
| API Gateway         | 8090 |
| Market Data Service | 8081 |
| Portfolio Service   | 8082 |