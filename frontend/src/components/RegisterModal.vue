<script setup>
import { reactive, watch } from 'vue'
import { useRegister } from '@/composables/useRegister'

const props = defineProps({
  open: { type: Boolean, required: true },
})
const emit = defineEmits(['close', 'success'])

const { register, loading, error, isSuccess } = useRegister()

const form = reactive({
  email: '',
  username: '',
  password: '',
})

// Reset the form whenever the modal is closed.
watch(
  () => props.open,
  (isOpen) => {
    if (!isOpen) {
      Object.assign(form, { email: '', username: '', password: '' })
    }
  },
)

async function onSubmit() {
  try {
    await register({ ...form })
    emit('success')
  } catch {
    /* error already surfaced via the composable's `error` ref */
  }
}

function onClose() {
  if (loading.value) return
  emit('close')
}
</script>

<template>
  <div v-if="open" class="modal-backdrop" @click.self="onClose">
    <div class="modal" role="dialog" aria-labelledby="register-title" aria-modal="true">
      <header class="modal-header">
        <h2 id="register-title">Create your account</h2>
        <button type="button" class="close" :disabled="loading" @click="onClose" aria-label="Close">
          ×
        </button>
      </header>

      <form class="modal-body" @submit.prevent="onSubmit">
        <label>
          <span>Email</span>
          <input v-model="form.email" type="email" required autocomplete="email" />
        </label>
        <label>
          <span>Username</span>
          <input v-model="form.username" type="text" required minlength="3" autocomplete="username" />
        </label>
        <label>
          <span>Password</span>
          <input v-model="form.password" type="password" required minlength="8" autocomplete="new-password" />
        </label>

        <p v-if="error" class="error" role="alert">{{ error }}</p>

        <div class="actions">
          <button type="button" class="btn-secondary" :disabled="loading" @click="onClose">
            Cancel
          </button>
          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? 'Creating…' : 'Create account' }}
          </button>
        </div>

        <p v-if="isSuccess" class="success">Account created. Redirecting…</p>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 8px;
  width: min(420px, 92vw);
  padding: 1.25rem 1.5rem;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}
.modal-header h2 {
  font-size: 1.1rem;
  margin: 0;
}
.close {
  background: none;
  border: 0;
  font-size: 1.4rem;
  cursor: pointer;
  color: var(--muted);
}
.modal-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.modal-body label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.9rem;
  color: var(--muted);
}
.modal-body input {
  padding: 0.5rem 0.7rem;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--bg);
  color: var(--text);
  font: inherit;
}
.actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  margin-top: 0.5rem;
}
.btn-primary,
.btn-secondary {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  border: 1px solid var(--border);
  font: inherit;
  cursor: pointer;
}
.btn-primary {
  background: var(--accent);
  color: #0d1117;
  border-color: var(--accent);
}
.btn-primary:disabled,
.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.error {
  color: var(--danger);
  font-size: 0.9rem;
  margin: 0;
}
.success {
  color: var(--accent);
  font-size: 0.9rem;
  margin: 0;
}
</style>
