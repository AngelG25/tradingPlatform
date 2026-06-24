import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
  },
  {
    path: '/callback',
    name: 'callback',
    component: () => import('@/views/CallbackView.vue'),
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  // Unauthenticated users go straight to Keycloak — no in-app login screen.
  if (!auth.isAuthenticated) {
    auth.login() // window.location.href → Keycloak
    return false  // cancel navigation; browser is leaving
  }
})