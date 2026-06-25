<# Keycloak login theme

Custom login page for the `trading-app` realm. Drop-in replacement for Keycloak's
default `keycloak.v2` login theme.

## Layout

```
keycloak-theme/
├── theme.properties              # registers this theme with Keycloak
├── login/
│   ├── login.ftl                # FreeMarker template — extends keycloak.v2's template.ftl
│   ├── resources/
│   │   ├── css/styles.css       # dark green/red theme styling (matches the SPA palette)
│   │   └── js/script.js         # small UX tweaks (autofocus first input)
│   └── messages/
│       └── messages_en.properties   # optional: override default strings
```

## How it works

`login.ftl` imports the parent theme's `template.ftl` and fills in two sections:

- `header` — renders the page title with a `<span class="brand">` for the green accent.
- `form` — renders `#kc-form-login` with `#username`, `#password`, optional `#rememberMe`, and the `.pf-m-primary` submit button.

`styles.css` overrides the IDs and classes that the parent template renders
(`#kc-content-wrapper`, `#kc-form-login`, `.pf-c-button.pf-m-primary`,
`.pf-c-form-control`, etc.) — so missing styles usually mean either the FTL is
not extending the parent, or the theme's `parent=` is wrong in `theme.properties`.

## Install into Keycloak (dev)

### Option A — copy into the running container

If Keycloak is running via `docker compose`, copy the theme into the mounted
directory:

```
cp -r keycloak-theme/* ../backend/keycloak-themes/trading-app/
```

then restart Keycloak (`docker compose restart keycloak`).

### Option B — volume mount

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

- `login.ftl` — page template. FreeMarker, extends `template.ftl` from the
  parent `keycloak.v2` theme. Available variables: `url`, `realm`, `client`,
  `login.username`, `register?isAvailable`, `messagesPerField`.
- `resources/css/styles.css` — visual styling. Palette tokens live in `:root`.
- `resources/js/script.js` — JS that runs on the page.
- `messages/messages_en.properties` — override individual strings (Keycloak reads
  the active locale's file).

## Cache note

FreeMarker templates and resources are cached by Keycloak. After changing any
`.ftl`, `messages_*.properties`, or `theme.properties` file, restart Keycloak
(`docker compose restart keycloak`). CSS/JS changes inside `resources/` are
served as static assets and pick up on the next page reload (hard refresh in
the browser if needed).

## Required identifiers

The form **must** keep `id="kc-form-login"` and the `name="username"` /
`name="password"` inputs — the parent's JS fallback depends on them.