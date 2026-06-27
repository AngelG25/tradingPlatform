<script setup>
import { computed } from 'vue'
import { RouterView } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import { useAuth } from '@/composables/useAuth'

const auth = useAuth()

const showLanding = computed(() => !auth.isAuthenticated.value)
</script>

<template>
  <div class="app">
    <DashboardView v-if="!showLanding" />
    <RouterView v-else />
  </div>
</template>

<style>
:root {
  color-scheme: dark;
  --bg: #0d1117;
  --surface: #161b22;
  --border: #30363d;
  --text: #c9d1d9;
  --muted: #8b949e;
  --accent: #3fb950;
  --danger: #f85149;
  --link: #58a6ff;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif;
}

html,
body,
#app {
  margin: 0;
  padding: 0;
  height: 100%;
  background: var(--bg);
  color: var(--text);
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
</style>

<style scoped>
.app {
  align-items: stretch;
  justify-content: center;
}
</style>
