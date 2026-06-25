import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

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
  // /callback is the destination of the auth flow — don't guard it.
  if (to.name === 'callback') return true

  const auth = useAuth()
  if (auth.isAuthenticated.value) return true

  auth.login() // window.location.assign → browser leaves
  return false  // cancel vue-router navigation; browser is leaving
})