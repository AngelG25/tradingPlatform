<script setup>
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

const auth = useAuth()

const username = computed(
  () => auth.user.value?.preferred_username
    ?? auth.user.value?.name
    ?? auth.user.value?.email
    ?? auth.user.value?.sub
    ?? 'me',
)

async function doLogin() {
  await auth.login()
}

async function doLogout() {
  await auth.logout()
}

// Register is a placeholder for now — backend registration is via
// POST /api/users/register, but the user-manager flow is not wired up yet.
function doRegister() {
  // intentionally no-op until registration is implemented
}
</script>

<template>
  <main class="screen">
    <header class="bar">
      <span class="brand">Trading Platform</span>

      <nav v-if="auth.isAuthenticated.value" class="me" @click="doLogout" role="button" tabindex="0">
        <span class="avatar" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="8" r="4" />
            <path d="M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6" />
          </svg>
        </span>
        <span class="me-name">{{ username }}</span>
      </nav>

      <nav v-else class="actions">
        <button type="button" class="btn btn-secondary" @click="doRegister">
          Register
        </button>
        <button type="button" class="btn btn-primary" @click="doLogin">
          Login
        </button>
      </nav>
    </header>

    <section class="hero">
      <h1 class="title">Trade smarter, in one place.</h1>
      <p class="lede">
        A simple trading platform that lets you keep an eye on the markets,
        track your portfolio and place orders — all from a single dashboard.
        Sign in to get started, or create an account in seconds.
      </p>
    </section>
  </main>
</template>

<style scoped>
.screen {
  max-width: 880px;
  margin: 0 auto;
  padding: 2.5rem 1.5rem;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif;
}

.bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand {
  font-weight: 600;
  font-size: 1.1rem;
  letter-spacing: 0.04em;
}

.actions {
  display: flex;
  gap: 0.6rem;
}

.btn {
  font: inherit;
  font-size: 0.9rem;
  font-weight: 500;
  letter-spacing: 0.04em;
  padding: 0.5rem 1.1rem;
  border-radius: 4px;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text);
  cursor: pointer;
  transition: border-color 120ms, background 120ms, color 120ms;
}

.btn-secondary:hover {
  border-color: var(--link);
  color: var(--link);
}

.btn-primary {
  background: var(--accent);
  border-color: var(--accent);
  color: #0d1117;
}

.btn-primary:hover {
  filter: brightness(1.1);
}

.me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.8rem 0.35rem 0.45rem;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--surface);
  color: var(--text);
  cursor: pointer;
  font-size: 0.9rem;
  letter-spacing: 0.02em;
  transition: border-color 120ms, color 120ms;
}

.me:hover {
  border-color: var(--danger);
  color: var(--danger);
}

.avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.6rem;
  height: 1.6rem;
  border-radius: 50%;
  background: var(--border);
  color: var(--text);
}

.me:hover .avatar {
  color: var(--danger);
}

.me-name {
  font-weight: 500;
  max-width: 12rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hero {
  margin-top: 4rem;
  max-width: 620px;
}

.title {
  font-size: 2.2rem;
  font-weight: 600;
  line-height: 1.2;
  margin: 0 0 1rem;
  color: var(--text);
}

.lede {
  font-size: 1.05rem;
  line-height: 1.6;
  color: var(--muted);
  margin: 0;
}
</style>
