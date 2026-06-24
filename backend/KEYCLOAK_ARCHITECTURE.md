# Keycloak Architecture & Connections

## Overview

Keycloak acts as the Authorization Server for the entire platform. It is infrastructure — like PostgreSQL or Kafka — not a microservice you write code for.

The platform uses the **Authorization Code + PKCE** flow (OAuth 2.1 compliant). This means:

- The user's password is entered **only** into Keycloak's own login UI and never transmitted to any service in this platform.
- The frontend is a public SPA that talks directly to Keycloak for the PKCE exchange. No backend component in this platform proxies the password or stores tokens.
- The SPA uses `Authorization: Bearer <access_token>` on every API call; the API Gateway validates the JWT locally against JWKS.

---

## Realms

A realm is an isolated space in Keycloak with its own users, clients, and roles.

| Realm | Purpose |
|---|---|
| `master` | Keycloak's built-in admin realm. Used only for administrative operations between backend services. Never exposed to end users. |
| `trading-app` | Your application realm. Contains your real users, their roles, and the clients the frontend uses to authenticate. |

**Rule of thumb:** end users live in `trading-app`. Admin clients that manage Keycloak live in `master`.

---

## Clients

A client in Keycloak represents an application that requests tokens.

| Client | Realm | Purpose |
|---|---|---|
| `trading-gateway-frontend` | `trading-app` | Public client used by the frontend to initiate the PKCE login flow |
| `user-manager-admin` | `master` | Used by `user-manager` to call the Keycloak Admin API and create users |

### trading-gateway-frontend configuration

```
Client authentication:   OFF      ← public client: no client_secret needed
Service accounts:        OFF
Direct access grants:    OFF      ← password grant is disabled (OAuth 2.1)
Standard flow:           ON       ← Authorization Code flow
PKCE:                    Required (S256 only, plain disallowed)
Valid redirect URIs:     http://localhost:5173/callback
                         http://localhost:3000/callback
Web origins:             http://localhost:5173
```

**Why public and no secret?**
This client lives in the browser. A `client_secret` in a SPA is not a secret — anyone can read it from DevTools or network requests. PKCE replaces the secret with a cryptographic challenge generated fresh for every login, making it safe for public clients.

### user-manager-admin configuration

```
Client authentication:   ON
Service accounts:        ON    ← required for client_credentials grant
Direct access grants:    OFF   ← this client never logs in as a user
```

**Service account roles** → assign `admin` role from `master` realm.
This gives the client permission to call the Keycloak Admin API.

The `user-manager-admin` client secret is provided via the `KEYCLOAK_ADMIN_CLIENT_SECRET` environment variable. The dev default lives in `application.yml`; production deployments must source it from a secrets manager.

---

## Roles

Roles are defined in the `trading-app` realm and assigned to users.

| Role | Description |
|---|---|
| `ROLE_USER` | Standard user. Can manage their own portfolio and view market data. |
| `ROLE_ADMIN` | Administrator. Can list all users, deactivate accounts, etc. |

Configure Keycloak to assign `ROLE_USER` automatically to every new user via **Default roles** in realm settings.

---

## Connection Flows

### User registration

```
Frontend
  │
  │  POST /api/users/register (no token required)
  │  { username, email, password, phone?, timezone? }
  ▼
API Gateway (8090)
  │  route: /api/users/** → user-manager
  │  security: permitAll for /api/users/register
  ▼
User Manager (8083)
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
  │
  │  3. User Manager persists profile (phone, timezone, keycloak_id) in local PostgreSQL.
  │     If the DB write fails, User Manager calls Keycloak to delete the user
  │     (compensating transaction) so the two stores stay in sync.
```

---

### User login (Authorization Code + PKCE)

The user types their credentials **directly into Keycloak's login page** (which can be fully
styled with a custom theme to match the application). No password ever reaches the frontend
or `user-manager`.

```
1. Frontend (SPA)
   │
   │  Generates code_verifier (32 random bytes, base64url)
   │  Computes code_challenge = BASE64URL(SHA256(code_verifier))
   │  Redirects the browser to Keycloak's authorization endpoint:
   │
   │  GET /realms/trading-app/protocol/openid-connect/auth
   │    ?client_id=trading-gateway-frontend
   │    &response_type=code
   │    &redirect_uri=http://localhost:5173/callback
   │    &code_challenge=<hash>
   │    &code_challenge_method=S256
   │    &scope=openid profile email
   │    &state=<csrf token>
   │    &nonce=<replay token>
   ▼
Keycloak (8080)
   │  Shows its login page (styled with custom theme).
   │  User types username + password here — nowhere else.
   │  On success, redirects browser to:
   │
   │  http://localhost:5173/callback?code=<auth_code>&state=<csrf token>
   ▼
2. Frontend (SPA — callback page)
   │
   │  Verifies state matches the stored csrf token (CSRF protection).
   │
   │  Exchanges the code directly with Keycloak (no backend involved):
   │  POST /realms/trading-app/protocol/openid-connect/token
   │    grant_type=authorization_code
   │    client_id=trading-gateway-frontend
   │    code=<auth_code>
   │    redirect_uri=http://localhost:5173/callback
   │    code_verifier=<original verifier>
   ▼
Keycloak (8080)
   │  Validates code + verifier pair.
   │  Returns { access_token, refresh_token, id_token, expires_in, ... }
   ▼
SPA
   │
   │  Stores access_token in memory (NOT localStorage).
   │  Uses Authorization: Bearer <access_token> for every API call.
   │
   └─ Frontend redirects to the home page.
```

**Token storage:** when the frontend is built, the recommended pattern is memory-only (or an
HttpOnly cookie issued by the SPA backend, if one exists later). `localStorage` exposes tokens
to XSS. The exact storage decision is orthogonal to this spec and will be revisited when the
SPA exists.

---

### Authenticated request

```
Frontend (SPA)
  │
  │  GET /api/market/prices
  │  Authorization: Bearer <access_token>   (attached by the SPA from in-memory storage)
  ▼
API Gateway (8090)
  │
  │  Validates JWT signature locally using Keycloak's public key
  │  (downloaded once on startup from the JWKS endpoint).
  │  No round-trip to Keycloak needed per request.
  │
  │  Forwards the request with the valid token to the target service.
  ▼
Market Data Service (8081)
```

If the `access_token` is expired, the API Gateway returns 401 and the SPA either refreshes
its tokens (see below) or restarts the PKCE flow.

**Token refresh (SPA ↔ Keycloak, no backend):**
```
SPA
  │
  │  POST /realms/trading-app/protocol/openid-connect/token
  │    grant_type=refresh_token
  │    client_id=trading-gateway-frontend
  │    refresh_token=<stored refresh_token>
  ▼
Keycloak (8080)
  │
  │  Returns { access_token, refresh_token, expires_in, ... } (rotated if configured)
  ▼
SPA updates its in-memory tokens. The user never notices the refresh happened.
```

If the `refresh_token` is expired or revoked, Keycloak returns 400/401 and the SPA discards
its tokens and restarts the login flow.

---

### User logout (direct, no backend)

```
SPA
  │
  │  Calls Keycloak's end_session_endpoint to terminate the SSO session:
  │  GET /realms/trading-app/protocol/openid-connect/logout
  │    id_token_hint=<id_token>
  │    post_logout_redirect_uri=http://localhost:5173/
  │
  │  Calls Keycloak's token revocation endpoint to revoke the refresh_token:
  │  POST /realms/trading-app/protocol/openid-connect/revoke
  │    token=<refresh_token>
  │    client_id=trading-gateway-frontend
  ▼
Keycloak (8080) → session and refresh token invalidated
  │
  └─ SPA discards tokens from memory and redirects to the login page.
```

No backend component in this platform is involved in logout.

---

## Security Rules

| Endpoint | Token required | Role required |
|---|---|---|
| `POST /api/users/register` | No | None — public |
| `GET /api/users/me` | Yes (Bearer JWT) | None — identifies caller from JWT `sub` claim |
| `GET /api/market/**` | Yes (Bearer JWT) | `ROLE_USER` |
| `GET /api/portfolio/**` | Yes (Bearer JWT) | `ROLE_USER` |
| `GET /api/admin/users` | Yes (Bearer JWT) | `ROLE_ADMIN` |

**Login and logout are not endpoints in this platform.** The SPA exchanges the PKCE code
directly with Keycloak, stores tokens in memory, and attaches `Authorization: Bearer
<access_token>` on every API call. Refresh, revocation, and end-session are SPA ↔ Keycloak
calls — no backend in this platform is involved.

---

## Key Concepts

**Why does the user type their password into Keycloak and not into the app's own form?**
OAuth 2.1 deprecates the password grant (`grant_type=password`) because it requires the
user's password to pass through the application's backend, where it could be logged,
captured, or leaked. With Authorization Code + PKCE, the password is entered into
Keycloak's own form and never leaves it. The application only receives a short-lived
authorization code, not the password.

**Can the Keycloak login page be customised?**
Yes. Keycloak supports fully custom login themes (HTML, CSS, JS via FreeMarker templates).
The user sees the application's own design; Keycloak's origin is not visible to them.

**Why PKCE instead of a client secret for the frontend client?**
A `client_secret` embedded in a SPA is not a secret — it can be extracted from DevTools or
network requests by anyone. PKCE generates a fresh cryptographic challenge for every login
attempt, binding the authorization code to the browser session that requested it. There is
nothing permanent to steal.

**Why HttpOnly cookies instead of storing tokens in memory or localStorage?**
`localStorage` and JS-accessible memory are vulnerable to XSS attacks — any injected script
can read and exfiltrate tokens. HttpOnly cookies are inaccessible to JavaScript entirely,
eliminating that attack surface. `SameSite=Strict` mitigates CSRF. (This tradeoff will be
revisited when the frontend is implemented; the spec currently leaves storage strategy open.)

**Why does the API Gateway never call Keycloak on every request?**
JWT uses asymmetric cryptography. Keycloak signs tokens with its private key. The Gateway
downloads the public key once on startup from the JWKS endpoint and verifies signatures
locally — no network round-trip needed per request.

**Why two separate clients instead of one?**
Principle of least privilege. `trading-gateway-frontend` can only authenticate users, never
manage them. `user-manager-admin` can only manage users, never act as a user. If one
client is compromised, the damage is contained.

**Why does user-manager call the master realm for the admin token?**
Because `user-manager-admin` client lives in the `master` realm. The token endpoint always
lives in the same realm as the client. The admin API call itself targets
`/admin/realms/trading-app/users` regardless of which realm issued the admin token.

**What happens if the refresh token expires or is revoked?**
The SPA receives a 400/401 from Keycloak on the refresh attempt, discards its in-memory
tokens, and restarts the PKCE login flow.