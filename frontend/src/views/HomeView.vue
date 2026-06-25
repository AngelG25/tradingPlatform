<script setup>
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

const auth = useAuth()
const username = computed(
  () => auth.user.value?.preferred_username ?? auth.user.value?.sub ?? 'unknown',
)

async function doLogout() {
  await auth.logout()
}
</script>

<template>
  <main class="screen">
    <header class="bar">
      <span class="brand">Trading Platform</span>
      <span class="version">v0.1</span>
    </header>

    <hr class="rule" />

    <section class="panel">
      <div class="row">
        <span class="label">signed in:</span>
        <span class="value">{{ username }}</span>
      </div>
      <div class="row">
        <span class="label">session:</span>
        <span class="dot" />
        <span class="value">active</span>
      </div>

      <button type="button" class="btn btn-danger" @click="doLogout">
        Logout
      </button>
    </section>
  </main>
</template>

<style scoped>
.screen {
  max-width: 720px;
  margin: 0 auto;
  padding: 2.5rem 1.5rem;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.bar {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  letter-spacing: 0.04em;
}

.brand {
  font-weight: 600;
}

.version {
  color: var(--muted);
  font-size: 0.85rem;
}

.rule {
  border: 0;
  border-top: 1px solid var(--border);
  margin: 0.75rem 0 1.5rem;
}

.panel {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.row {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.label {
  color: var(--muted);
  min-width: 5.5rem;
}

.value {
  color: var(--text);
}

.dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 6px var(--accent);
}

.btn {
  margin-top: 1.25rem;
  align-self: flex-start;
  font-family: inherit;
  font-size: 0.95rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  padding: 0.55rem 1.2rem;
  border-radius: 4px;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text);
  cursor: pointer;
}

.btn:hover {
  border-color: var(--danger);
  color: var(--danger);
}

.btn-danger:hover {
  background: rgba(248, 81, 73, 0.08);
}
</style>