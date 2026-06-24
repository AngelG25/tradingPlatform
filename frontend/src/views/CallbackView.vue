<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
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
  <div>
    <h1>Signing you in…</h1>
    <p v-if="error">Login failed: {{ error }}</p>
    <p v-else>Exchanging authorization code for tokens.</p>
  </div>
</template>