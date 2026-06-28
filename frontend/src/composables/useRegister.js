// src/composables/useRegister.js
import { ref } from 'vue'

const GATEWAY_BASE = import.meta.env.VITE_API_GATEWAY ?? 'http://localhost:8090'
const REGISTER_PATH = '/api/users/register'

/**
 * useRegister — calls the backend registration endpoint via the API gateway.
 * Returns reactive loading / error / success state and a register() function.
 */
export function useRegister() {
  const loading = ref(false)
  const error = ref(null)
  const isSuccess = ref(false)

  async function register({ email, username, password }) {
    loading.value = true
    error.value = null
    isSuccess.value = false

    try {
      const res = await fetch(`${GATEWAY_BASE}${REGISTER_PATH}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, username, password }),
      })

      if (!res.ok) {
        // Try to extract a server-provided message; fall back to status text.
        let message = res.statusText || `Registration failed (${res.status})`
        try {
          const data = await res.json()
          if (data?.message) message = data.message
        } catch {
          /* body was not JSON — keep the status-based message */
        }
        throw new Error(message)
      }

      isSuccess.value = true
      return await res.json().catch(() => ({}))
    } catch (e) {
      // fetch throws TypeError("Failed to fetch") for both network failures
      // and CORS preflight failures — surface a more useful hint to the user.
      const raw = e instanceof Error ? e.message : 'Unknown error'
      error.value =
        raw === 'Failed to fetch'
          ? 'Failed to reach the API gateway (CORS or network). Check that the gateway at http://localhost:8090 is running and accepts requests from this origin.'
          : raw
      throw e
    } finally {
      loading.value = false
    }
  }

  return { register, loading, error, isSuccess }
}
