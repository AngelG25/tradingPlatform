<# Keycloak login theme

Custom login page for the `trading-app` realm. Drop-in replacement for Keycloak's
default `keycloak.v2` login theme.

## Layout

```
keycloak-theme/
├── theme.properties              # registers this theme with Keycloak
├── login/
│   ├── login.ftl                # FreeMarker template (the actual page)
│   ├── resources/
│   │   ├── css/styles.css       # theme styling
│   │   └── js/script.js         # small UX tweaks (focus first input)
│   └── messages/
│       └── messages_en.properties   # optional: override default strings
```

## Install into Keycloak (dev)

### Option A — mount into the running Keycloak container

If you started Keycloak via `docker compose`, the theme directory on the host is
mounted into `/opt/keycloak/themes` (see `../backend/docker-compose.yml`).
Copy this whole `keycloak-theme/` directory there:

```
cp -r keycloak-theme/* ../backend/keycloak-themes/trading-app/
```

then restart Keycloak (`docker compose restart keycloak`).

### Option B — add a volume mount

Edit `../backend/docker-compose.yml` so Keycloak mounts the theme directly:

```yaml
services:
  keycloak:
    volumes:
      - ./keycloak-themes:/opt/keycloak/themes
```

Put the theme under `backend/keycloak-themes/trading-app/` matching the layout
above, then `docker compose restart keycloak`.

## Activate the theme

In the Keycloak admin console (`http://localhost:8080`, realm `master`):

1. Switch to the `trading-app` realm.
2. **Realm settings → Themes → Login theme**: select `trading-app`.
3. Save.

Or via the CLI:

```
docker exec -it keycloak /opt/keycloak/bin/kc.sh \
  update login-theme --realm trading-app trading-app
```

## What you can edit

- `login.ftl` — the page template. Uses FreeMarker + the variables Keycloak
  provides (`url`, `realm`, `client`, `login.username`, `register?isAvailable`, etc.).
- `resources/css/styles.css` — visual styling.
- `resources/js/script.js` — JS that runs on the page.
- `messages/messages_en.properties` — copy and override individual strings
  (Keycloak reads the active locale's file).

## Notes

- `theme.properties` must declare `parent=keycloak.v2` so missing templates
  fall back to Keycloak's built-in set.
- FreeMarker templates are cached. After changing `.ftl` files, restart Keycloak
  or use `bin/kc.sh build` (in dev mode Keycloak picks up changes on restart).
- The form must keep `id="kc-form-login"` and the `name="username"` / `name="password"`
  inputs — Keycloak's own `loginForm.ftl` expects these for the JS fallback.