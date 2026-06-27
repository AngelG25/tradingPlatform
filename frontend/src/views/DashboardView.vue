<script setup>
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

const auth = useAuth()

const username = computed(
  () =>
    auth.user.value?.preferred_username ??
    auth.user.value?.name ??
    auth.user.value?.email ??
    auth.user.value?.sub ??
    'me',
)

async function onLogout() {
  await auth.logout()
}
</script>

<template>
  <div class="dash">
    <header class="dash-bar">
      <span class="dash-brand">Trading Platform</span>
      <button
        v-if="auth.isAuthenticated.value"
        type="button"
        class="me"
        @click="onLogout"
      >
        <span class="avatar" aria-hidden="true">
          <svg
            viewBox="0 0 24 24"
            width="16"
            height="16"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <circle cx="12" cy="8" r="4" />
            <path d="M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6" />
          </svg>
        </span>
        <span class="me-name">{{ username }}</span>
      </button>
    </header>

    <main class="dash-main">
      <h1 class="dash-title">Welcome, {{ username }} 👋</h1>
      <p class="dash-lede">
        You're signed in. Below are quick shortcuts to your workspace.
      </p>

      <section class="grid">
        <article class="card">
          <h2>Portfolio</h2>
          <p>Your holdings, balances and P/L — coming soon.</p>
        </article>
        <article class="card">
          <h2>Orders</h2>
          <p>Place, amend and cancel orders — coming soon.</p>
        </article>
        <article class="card">
          <h2>Markets</h2>
          <p>Live prices and watchlist — coming soon.</p>
        </article>
      </section>
    </main>
  </div>
</template>

<style scoped>
.dash {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

.dash-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border);
  background: var(--surface);
}

.dash-brand {
  font-weight: 600;
  font-size: 1.1rem;
  letter-spacing: 0.04em;
  color: var(--text);
}

.me {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.8rem 0.35rem 0.45rem;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--surface);
  color: var(--text);
  cursor: pointer;
  font: inherit;
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

.me-name {
  font-weight: 500;
  max-width: 12rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dash-main {
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
  padding: 2.5rem 1.5rem;
  flex: 1;
}

.dash-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin: 0 0 0.5rem;
  color: var(--text);
}

.dash-lede {
  margin: 0 0 2rem;
  color: var(--muted);
  font-size: 1.05rem;
  line-height: 1.6;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 1.25rem;
}

.card h2 {
  font-size: 1.05rem;
  font-weight: 600;
  margin: 0 0 0.5rem;
  color: var(--text);
}

.card p {
  margin: 0;
  color: var(--muted);
  font-size: 0.95rem;
  line-height: 1.5;
}
</style>
