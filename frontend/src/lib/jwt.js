// JWT payload decoder. Reuses base64urlDecode from pkce.js.
import { base64urlDecode } from './pkce'

export function parseJwt(token) {
  if (!token) return null
  try {
    const payload = token.split('.')[1]
    const json = new TextDecoder().decode(base64urlDecode(payload))
    return JSON.parse(json)
  } catch {
    return null
  }
}