// Plain Keycloak constants. No Vue, no functions.

export const KEYCLOAK_URL = 'http://localhost:8080'
export const REALM = 'trading-app'
export const CLIENT_ID = 'trading-gateway-frontend'
export const REDIRECT_URI = `${window.location.origin}/callback`
export const POST_LOGOUT_REDIRECT_URI = `${window.location.origin}/`
export const SCOPES = 'openid profile email'