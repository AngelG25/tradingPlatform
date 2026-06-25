// Pure browser-native PKCE + base64url helpers. No Vue, no Keycloak constants.

export function base64urlEncode(bytes) {
  let str = ''
  for (const b of new Uint8Array(bytes)) str += String.fromCharCode(b)
  return btoa(str).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

export function base64urlDecode(str) {
  str = str.replace(/-/g, '+').replace(/_/g, '/')
  while (str.length % 4) str += '='
  const bin = atob(str)
  const out = new Uint8Array(bin.length)
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i)
  return out
}

export function generateCodeVerifier() {
  const bytes = new Uint8Array(32) // 43-128 chars after base64url
  crypto.getRandomValues(bytes)
  return base64urlEncode(bytes)
}

export async function generateCodeChallenge(verifier) {
  const data = new TextEncoder().encode(verifier)
  const digest = await crypto.subtle.digest('SHA-256', data)
  return base64urlEncode(digest)
}

export function generateRandomString(length = 16) {
  const bytes = new Uint8Array(length)
  crypto.getRandomValues(bytes)
  return base64urlEncode(bytes)
}