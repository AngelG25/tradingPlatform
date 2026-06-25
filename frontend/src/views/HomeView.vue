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
  <div>
    <h1>Home</h1>
    <p>Signed in as: <strong>{{ username }}</strong></p>
    <button type="button" @click="doLogout">Logout</button>
  </div>
</template>