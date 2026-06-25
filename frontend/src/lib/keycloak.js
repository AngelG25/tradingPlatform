// High-level Keycloak HTTP client. Stateless — no module-level refs.
// Wraps PKCE helpers from ./pkce and constants from ./authConfig.

import {
  generateCodeVerifier,
  generateCodeChallenge,
  generateRandomString,
} from './pkce'
import {
  KEYCLOAK_URL,
  REALM,
  CLIENT_ID,
  REDIRECT_URI,
  POST_LOGOUT_REDIRECT_URI,
  SCOPES,
} from './authConfig'

const authEndpoint = () =>
  `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/auth`
const tokenEndpoint = () =>
  `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`
const endSessionEndpoint = () =>
  `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/logout`
const revokeEndpoint = () =>
  `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/revoke`

// ---- Authorization redirect ----

export async function buildAuthorizationUrl() {
  const verifier = generateCodeVerifier()
  const challenge = await generateCodeChallenge(verifier)
  const state = generateRandomString()
  const nonce = generateRandomString()

  // Stash for the callback to pick up. sessionStorage — transient, consumed once.
  sessionStorage.setItem('pkce_verifier', verifier)
  sessionStorage.setItem('pkce_state', state)
  sessionStorage.setItem('pkce_nonce', nonce)

  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    response_type: 'code',
    redirect_uri: REDIRECT_URI,
    code_challenge: challenge,
    code_challenge_method: 'S256',
    scope: SCOPES,
    state,
    nonce,
  })
  return `${authEndpoint()}?${params.toString()}`
}

export function readStoredPkce() {
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
  const { verifier } = readStoredPkce()
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

export async function revokeToken(token) {
  const body = new URLSearchParams({ token, client_id: CLIENT_ID })
  const res = await fetch(revokeEndpoint(), {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  })
  if (!res.ok) throw new Error(`revoke failed: ${res.status}`)
}

export function buildEndSessionUrl(idToken) {
  const params = new URLSearchParams({
    id_token_hint: idToken,
    post_logout_redirect_uri: POST_LOGOUT_REDIRECT_URI,
  })
  return `${endSessionEndpoint()}?${params.toString()}`
}