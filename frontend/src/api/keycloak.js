// Keycloak PKCE helpers — Authorization Code + S256.
// All tokens stay in memory; never written to localStorage.

const KEYCLOAK_URL = 'http://localhost:8080'
const REALM = 'trading-app'
const CLIENT_ID = 'trading-gateway-frontend'
const REDIRECT_URI = `${window.location.origin}/callback`
const SCOPE = 'openid profile email'

const authEndpoint = () =>
  `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/auth`
const tokenEndpoint = () =>
  `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`
const endSessionEndpoint = () =>
  `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/logout`
const revokeEndpoint = () =>
  `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/revoke`

// ---- base64url helpers (browser-native, no Buffer) ----

function base64urlEncode(bytes) {
  let str = ''
  for (const b of new Uint8Array(bytes)) str += String.fromCharCode(b)
  return btoa(str).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

function base64urlDecode(str) {
  str = str.replace(/-/g, '+').replace(/_/g, '/')
  while (str.length % 4) str += '='
  const bin = atob(str)
  const out = new Uint8Array(bin.length)
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i)
  return out
}

// ---- PKCE ----

export function generateCodeVerifier() {
  const bytes = new Uint8Array(32) // 43-128 chars after b64url
  crypto.getRandomValues(bytes)
  return base64urlEncode(bytes)
}

export async function generateCodeChallenge(verifier) {
  const data = new TextEncoder().encode(verifier)
  const digest = await crypto.subtle.digest('SHA-256', data)
  return base64urlEncode(digest)
}

function randomString(len = 16) {
  const bytes = new Uint8Array(len)
  crypto.getRandomValues(bytes)
  return base64urlEncode(bytes)
}

// ---- Authorization redirect ----

export async function buildAuthorizationUrl() {
  const verifier = generateCodeVerifier()
  const challenge = await generateCodeChallenge(verifier)
  const state = randomString()
  const nonce = randomString()

  // stash for the callback to pick up
  sessionStorage.setItem('pkce_verifier', verifier)
  sessionStorage.setItem('pkce_state', state)
  sessionStorage.setItem('pkce_nonce', nonce)

  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    response_type: 'code',
    redirect_uri: REDIRECT_URI,
    code_challenge: challenge,
    code_challenge_method: 'S256',
    scope: SCOPE,
    state,
    nonce,
  })
  return `${authEndpoint()}?${params.toString()}`
}

export function getStoredPkce() {
  return {
    verifier: sessionStorage.getItem('pkce_verifier'),
    state: sessionStorage.getItem('pkce_state'),
    nonce: sessionStorage.getItem('pkce_nonce'),
  }
}

export function clearStoredPkce() {
  sessionStorage.removeItem('pkce_verifier')
  sessionStorage.removeItem('pkce_state')
  sessionStorage.removeItem('pkce_nonce')
}

// ---- Token exchange ----

export async function exchangeCodeForTokens(code) {
  const { verifier } = getStoredPkce()
  const body = new URLSearchParams({
    grant_type: 'authorization_code',
    client_id: CLIENT_ID,
    code,
    redirect_uri: REDIRECT_URI,
    code_verifier: verifier,
  })
  const res = await fetch(tokenEndpoint(), {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  })
  if (!res.ok) throw new Error(`token exchange failed: ${res.status}`)
  return res.json()
}

export async function refreshTokens(refreshToken) {
  const body = new URLSearchParams({
    grant_type: 'refresh_token',
    client_id: CLIENT_ID,
    refresh_token: refreshToken,
  })
  const res = await fetch(tokenEndpoint(), {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  })
  if (!res.ok) throw new Error(`refresh failed: ${res.status}`)
  return res.json()
}

// ---- Logout ----

export async function logoutKeycloak(idToken, refreshToken) {
  // end SSO session at Keycloak
  const params = new URLSearchParams({
    id_token_hint: idToken,
    post_logout_redirect_uri: `${window.location.origin}/`,
  })
  // browser navigation — not a fetch
  window.location.href = `${endSessionEndpoint()}?${params.toString()}`

  // best-effort refresh token revocation (fire and forget)
  if (refreshToken) {
    const body = new URLSearchParams({
      token: refreshToken,
      client_id: CLIENT_ID,
    })
    fetch(revokeEndpoint(), {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body,
    }).catch(() => {})
  }
}

export { KEYCLOAK_URL, REALM, CLIENT_ID, REDIRECT_URI }