# PKCE Login Implementation Plan

Status: **NOT STARTED** — deferred. Reference doc only.

Goal: implement Keycloak Authorization Code + PKCE login for the trading-platform SPA. No CSS, functionality only. Registration is out of scope for this iteration.

## Decisions (confirmed)

- **PKCE crypto:** hand-rolled with Web Crypto API (`crypto.getRandomValues`, `crypto.subtle.digest`). No PKCE library.
- **State management:** composable (`useAuth()`) with module-scoped reactive state. No Pinia.
- **Token storage:** in-memory only. Transient PKCE values (`verifier`, `state`, `nonce`) live in `sessionStorage` — they're consumed once and don't grant access.
- **Routing:** add `vue-router` now. Routes: `/`, `/callback`, `/home`.

## Architecture

```
User clicks "Login" (HomeView)
        │
        ▼
useAuth().login()
  ├─ generate code_verifier (32 random bytes, base64url)
  ├─ compute code_challenge = BASE64URL(SHA256(code_verifier))
  ├─ generate state (CSRF) and nonce (replay)
  ├─ store {verifier, state, nonce} in sessionStorage (transient)
  └─ window.location.assign(<keycloak /auth URL with challenge>)

Keycloak → browser → http://localhost:5173/callback?code=...&state=...
        │
        ▼
CallbackView (mounted)
  ├─ verify state matches stored state
  ├─ POST /token with code + verifier
  ├─ receive {access_token, refresh_token, id_token}
  ├─ hand tokens to useAuth() (kept in memory)
  ├─ clear sessionStorage
  └─ router.replace('/home')

HomeView
  ├─ if no access_token → redirect to '/'
  └─ otherwise: show "Logged in" + a logout button
```

## Files to add / modify

```
src/
├── main.js                          MODIFY: install router
├── App.vue                          MODIFY: <router-view/>
├── router/index.js                  NEW: 3 routes
├── views/HomeView.vue               NEW: login button OR post-login landing
├── views/CallbackView.vue           NEW: handles token exchange
├── composables/useAuth.js           NEW: tokens + login/logout
├── lib/pkce.js                      NEW: Web Crypto helpers
└── lib/authConfig.js                NEW: Keycloak constants
```

## Component breakdown

### `src/lib/pkce.js`
Pure functions, no Vue. Exports:
- `generateCodeVerifier()` → base64url string (43–128 chars)
- `generateCodeChallenge(verifier)` → base64url SHA-256 digest
- `generateRandomString(length)` → base64url, used for `state` and `nonce`

Uses `crypto.getRandomValues` + `crypto.subtle.digest`.

### `src/lib/authConfig.js`
```js
export const KEYCLOAK_URL = 'http://localhost:8080'
export const REALM = 'trading-app'
export const CLIENT_ID = 'trading-gateway-frontend'
export const REDIRECT_URI = 'http://localhost:5173/callback'
export const SCOPES = 'openid profile email'
```

### `src/composables/useAuth.js`
Module-scoped reactive state (singleton pattern). Exports:
- `accessToken`, `refreshToken`, `idToken`, `isAuthenticated` (computed)
- `login()` — generate verifier/challenge/state/nonce, build auth URL, store transient values in `sessionStorage`, `window.location.assign` to Keycloak
- `handleCallback(searchParams)` — verify state, exchange code at `/token`, populate tokens in memory, clear sessionStorage
- `logout()` — call Keycloak `end_session_endpoint` with `id_token_hint` + `post_logout_redirect_uri`, then clear in-memory tokens

### `src/router/index.js`
3 routes:
- `/` → `HomeView` (login button)
- `/callback` → `CallbackView` (no auth guard; it's the destination of the auth flow)
- `/home` → `HomeView` post-login variant (auth guard redirects to `/` if no token)

### `src/views/HomeView.vue`
Checks `isAuthenticated`. If false: `<button>Login</button>` calling `useAuth().login()`. If true: text "Logged in" + `<button>Logout</button>`.

### `src/views/CallbackView.vue`
`onMounted(async () => { await useAuth().handleCallback(...); router.replace('/home') })`. Renders "Completing login..." while waiting, error message on failure.

## Keycloak endpoints (from `../backend/KEYCLOAK_ARCHITECTURE.md`)

- Authorization: `GET /realms/trading-app/protocol/openid-connect/auth`
- Token: `POST /realms/trading-app/protocol/openid-connect/token`
- End-session: `GET /realms/trading-app/protocol/openid-connect/logout`
- Revocation: `POST /realms/trading-app/protocol/openid-connect/revoke`

PKCE method: **S256 only**. State and nonce both required. Redirect URI must match exactly: `http://localhost:5173/callback`.

## Error handling

- **State mismatch (CSRF)** → log, redirect to `/`, generic "Login failed" message.
- **Token endpoint 4xx** → log response body, redirect to `/`, generic message. No leaky details.
- **Network failure during `/token`** → retry once, then fail with generic message.
- **Stale `/callback` revisit** → Keycloak errors; treat as failure.
- **Logout failure** → clear local tokens anyway. Don't block logout on Keycloak availability.

No global error component. `console.error` + a `<p>` on HomeView for the rare failure case.

## Out of scope (this iteration)

- Tests (no test runner yet — add Vitest as a separate decision)
- API client wrapper (`useApi()` for attaching Bearer tokens)
- Token refresh logic (SPA does refresh in a later iteration)
- Registration flow (`POST /api/users/register` is public — separate UI later)
- CSS, styling, layout
- Pinia, Vuex, or any other state lib

## Manual verification checklist

When implementation begins:

1. `npm run dev` — dev server starts on `:5173`
2. Open `http://localhost:5173` — see "Login" button
3. Click "Login" — redirects to Keycloak (`localhost:8080`)
4. Log in with a user from `trading-app` realm
5. Browser redirects to `http://localhost:5173/callback?code=...&state=...`
6. Briefly see "Completing login..." then redirect to `/home`
7. `/home` shows "Logged in" + "Logout" button
8. Click "Logout" → redirected to Keycloak end-session, then back to `/`
9. DevTools network tab: confirm `/token` POST succeeded, no tokens in `localStorage`
10. DevTools application tab: confirm `sessionStorage` is empty after callback completes