<script setup>
import { ref } from 'vue'
import { useAuth } from '@/composables/useAuth'
import RegisterModal from '@/components/RegisterModal.vue'

const showRegister = ref(false)
const { login } = useAuth()

function doRegister() {
  showRegister.value = true
}

function doLogin() {
  login()
}

// Registration only creates the account in the backend; it does NOT
// authenticate the user. Kick off the PKCE flow with the just-registered
// username as login_hint so Keycloak pre-fills the login form. The user
// only has to type their password.
function onRegisterSuccess(username) {
  showRegister.value = false
  login({ loginHint: username })
}
</script>

<template>
  <main class="screen">
    <header class="bar">
      <span class="brand">Trading Platform</span>

      <nav class="actions">
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

  <RegisterModal :open="showRegister" @close="showRegister = false" @success="onRegisterSuccess" />
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
