# Keycloak Architecture & Connections

## Overview

Keycloak acts as the Authorization Server for the entire platform. It is infrastructure — like PostgreSQL or Kafka — not a microservice you write code for.

---

## Realms

A realm is an isolated space in Keycloak with its own users, clients, and roles. Think of it as a tenant.

| Realm | Purpose |
|---|---|
| `master` | Keycloak's built-in admin realm. Used only for administrative operations between backend services. Never exposed to end users. |
| `trading-app` | Your application realm. Contains your real users, their roles, and the clients your frontend uses to authenticate. |

**Rule of thumb**: end users live in `trading-app`. Admin clients that manage Keycloak live in `master`.

---

## Clients

A client in Keycloak represents an application that requests tokens. Each application that interacts with Keycloak needs its own client.

| Client | Realm | Purpose |
|---|---|---|
| `trading-gateway` | `trading-app` | Used by the frontend to authenticate users and obtain JWT tokens |
| `user-manager-admin` | `master` | Used by `user-service` to call the Keycloak Admin API and create users |

### trading-gateway configuration
```
Client authentication:   ON
Service accounts:        ON
Direct access grants:    ON
Standard flow:           ON
Valid redirect URIs:     http://localhost:8090/*
```

### user-manager-admin configuration
```
Client authentication:   ON
Service accounts:        ON   ← required for client_credentials grant
Direct access grants:    OFF  ← this client never logs in as a user
```

**Service account roles** → assign `admin` role from `master` realm.
This gives the client permission to call the Keycloak Admin API.

---

## Roles

Roles are defined in the `trading-app` realm and assigned to users.

| Role | Description |
|---|---|
| `ROLE_USER` | Standard user. Can manage their own portfolio and view market data. |
| `ROLE_ADMIN` | Administrator. Can list all users, deactivate accounts, etc. |

Configure Keycloak to assign `ROLE_USER` automatically to every new user via **Default roles** in realm settings.

---

## Connection Flow

### User registration

```
Frontend
  │
  │  POST /api/users/register (no token required)
  ▼
API Gateway (8090)
  │  route: /api/users/** → user-service
  │  security: permitAll for /api/users/register
  ▼
User Service (8083)
  │
  │  1. POST /realms/master/protocol/openid-connect/token
  │     grant_type=client_credentials
  │     client_id=user-manager-admin
  │     client_secret=***
  ▼
Keycloak (8080) → returns admin JWT
  │
  │  2. POST /admin/realms/trading-app/users
  │     Authorization: Bearer <admin JWT>
  │     { username, email, password }
  ▼
Keycloak (8080) → creates user in trading-app realm
```

### User login

```
Frontend
  │
  │  POST /realms/trading-app/protocol/openid-connect/token
  │  grant_type=password
  │  client_id=trading-gateway
  │  client_secret=***
  │  username=user@trading.com
  │  password=***
  ▼
Keycloak (8080) → returns JWT with roles
  │
  └─ Frontend stores JWT and uses it for every subsequent request
```

### Authenticated request

```
Frontend
  │
  │  GET /api/market/prices
  │  Authorization: Bearer <JWT>
  ▼
API Gateway (8090)
  │  1. Downloads Keycloak public key once on startup from:
  │     /realms/trading-app/.well-known/openid-configuration
  │  2. Validates JWT signature locally — no round trip to Keycloak
  │  3. Checks expiry (exp claim) and roles
  ▼
Market Data Service (8081)
```

---

## Security rules

| Endpoint | Token required | Role required |
|---|---|---|
| `POST /api/users/register` | No | None — public |
| `GET /api/market/**` | Yes | `ROLE_USER` |
| `GET /api/portfolio/**` | Yes | `ROLE_USER` |
| `GET /api/admin/users` | Yes | `ROLE_ADMIN` |

---

## Key concepts

**Why does user-service call master realm for the token?**
Because `user-manager-admin` client lives in the `master` realm. The token endpoint always lives in the same realm as the client.

**Why does user-service create users in trading-app realm?**
Because that is where your real users live. The admin API call targets `/admin/realms/trading-app/users` regardless of which realm issued the admin token.

**Why does API Gateway never call Keycloak per request?**
JWT uses asymmetric cryptography. Keycloak signs tokens with its private key. The Gateway downloads the public key once on startup and verifies signatures locally — no network call needed per request.

**Why two separate clients instead of one?**
Principle of least privilege. `trading-gateway` should only be able to authenticate users, never manage them. `user-manager-admin` should only be able to manage users, never act as a user. If one secret is compromised, the damage is contained.
