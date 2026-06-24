import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  buildAuthorizationUrl,
  exchangeCodeForTokens,
  refreshTokens,
  logoutKeycloak,
  getStoredPkce,
  clearStoredPkce,
} from '@/api/keycloak'

// Tokens held in module-level refs (not localStorage). They survive across
// navigation within the SPA but vanish on a full page reload — by design.
const accessToken = ref(null)
const refreshToken = ref(null)
const idToken = ref(null)
const expiresAt = ref(0) // ms epoch
const user = ref(null)   // parsed id_token claims

function parseJwt(token) {
  if (!token) return null
  try {
    const payload = token.split('.')[1]
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(json)
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const isAuthenticated = computed(() => !!accessToken.value && Date.now() < expiresAt.value)

  async function login() {
    const url = await buildAuthorizationUrl()
    window.location.href = url
  }

  async function handleCallback(code, state) {
    const { state: expectedState } = getStoredPkce()
    if (!expectedState || state !== expectedState) {
      throw new Error('state mismatch — possible CSRF')
    }
    const tokens = await exchangeCodeForTokens(code)
    setTokens(tokens)
    clearStoredPkce()
    return tokens
  }

  function setTokens(tokens) {
    accessToken.value = tokens.access_token
    refreshToken.value = tokens.refresh_token ?? null
    idToken.value = tokens.id_token ?? null
    expiresAt.value = Date.now() + (tokens.expires_in ?? 0) * 1000
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
    accessToken.value = null
    refreshToken.value = null
    idToken.value = null
    expiresAt.value = 0
    user.value = null
    if (it) {
      logoutKeycloak(it, rt)
    } else {
      window.location.href = '/'
    }
  }

  function getAccessToken() {
    return accessToken.value
  }

  return {
    // state (refs exposed for templates)
    user,
    isAuthenticated,
    // actions
    login,
    handleCallback,
    refresh,
    logout,
    getAccessToken,
  }
})