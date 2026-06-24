<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const username = computed(() => auth.user?.preferred_username ?? auth.user?.sub ?? 'unknown')

async function doLogout() {
  await auth.logout()
}
</script>

<template>
  <div>
    <h1>Home</h1>
    <p>Signed in as: <strong>{{ username }}</strong></p>
    <button type="button" @click="doLogout">Logout</button>
  </div>
</template>