<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const auth = useAuth()
const error = ref(null)

onMounted(async () => {
  const params = route.query

  if (params.error) {
    error.value = params.error_description || params.error
    return
  }
  if (!params.code || !params.state) {
    error.value = 'missing code or state in callback'
    return
  }

  try {
    await auth.handleCallback(params.code, params.state)
    router.replace({ name: 'home' })
  } catch (e) {
    error.value = e.message
  }
})
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
        <span class="prompt">{{ error ? '!' : '>' }}</span>
        <span :class="error ? 'msg msg-error' : 'msg msg-pending'">
          {{ error ? 'Login failed' : 'Signing you in' }}
          <span class="dots">…</span>
        </span>
      </div>

      <p v-if="error" class="detail">Login failed: {{ error }}</p>
      <p v-else class="detail">Exchanging authorization code for tokens.</p>
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
  align-items: baseline;
  gap: 0.6rem;
}

.prompt {
  color: var(--accent);
  font-weight: 700;
}

.msg {
  font-size: 1.1rem;
  letter-spacing: 0.04em;
}

.msg-pending {
  color: var(--text);
}

.msg-error {
  color: var(--danger);
}

.dots {
  color: var(--muted);
  animation: blink 1.2s steps(4, end) infinite;
}

@keyframes blink {
  0%, 25% { opacity: 0.2; }
  50% { opacity: 1; }
  100% { opacity: 0.2; }
}

.detail {
  color: var(--muted);
  margin: 0.25rem 0 0 1.2rem;
  font-size: 0.9rem;
}
</style>