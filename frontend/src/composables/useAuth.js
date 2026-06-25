// Auth state singleton. Module-scoped refs survive navigation within the SPA
// but vanish on a full page reload — by design.

import { ref, computed } from 'vue'
import {
  buildAuthorizationUrl,
  readStoredPkce,
  clearStoredPkce,
  exchangeCodeForTokens,
  refreshTokens,
  revokeToken,
  buildEndSessionUrl,
} from '@/lib/keycloak'
import { parseJwt } from '@/lib/jwt'

const accessToken = ref(null)
const refreshToken = ref(null)
const idToken = ref(null)
const expiresAt = ref(0) // ms epoch; 0 = expired/unauthenticated
const user = ref(null)   // parsed id_token claims

export function useAuth() {
  const isAuthenticated = computed(
    () => !!accessToken.value && Date.now() < expiresAt.value,
  )

  async function login() {
    const url = await buildAuthorizationUrl()
    window.location.assign(url)
  }

  async function handleCallback(code, state) {
    const { state: expectedState } = readStoredPkce()
    if (!expectedState || state !== expectedState) {
      throw new Error('state mismatch — possible CSRF')
    }
    const tokens = await exchangeCodeForTokens(code)
    setTokens(tokens)
    clearStoredPkce()
    return tokens
  }

  function setTokens(tokens) {
    accessToken.value = tokens.access_token ?? null
    refreshToken.value = tokens.refresh_token ?? null
    idToken.value = tokens.id_token ?? null
    // expires_in is required: missing/non-positive → leave expiresAt at 0
    // so isAuthenticated is false rather than falsely authenticated.
    const ttl = Number(tokens.expires_in)
    expiresAt.value = ttl > 0 ? Date.now() + ttl * 1000 : 0
    user.value = parseJwt(tokens.id_token)
  }

  async function refresh() {
    if (!refreshToken.value) throw new Error('no refresh token')
    const tokens = await refreshTokens(refreshToken.value)
    setTokens(tokens)
    return tokens
  }

  async function logout() {
    const rt = refreshToken.value
    const it = idToken.value

    // Clear local state up front — logout should never be blocked by a
    // transient network issue.
    accessToken.value = null
    refreshToken.value = null
    idToken.value = null
    expiresAt.value = 0
    user.value = null

    if (!it) {
      window.location.assign('/')
      return
    }

    // Best-effort revoke. Don't block logout if it fails.
    if (rt) {
      try {
        await revokeToken(rt)
      } catch (e) {
        console.warn('revoke failed:', e)
      }
    }

    window.location.assign(buildEndSessionUrl(it))
  }

  return {
    isAuthenticated,
    user,
    accessToken,
    login,
    handleCallback,
    refresh,
    logout,
  }
}