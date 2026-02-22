<script setup lang="ts">
import { defineProps, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps<{
  show: boolean
  winner: string
  result: 'win' | 'lose' | 'draw' | null
}>()

const emit = defineEmits<{
  (e: 'restart'): void
}>()

const router = useRouter()

function handleRestart() {
  emit('restart')
}

function goHome() {
  router.push('/')
}

watch(
  () => props.show,
  (isOpen) => {
    if (isOpen) {
      document.body.classList.add('no-scroll')
    } else {
      removeScrollLock()
    }
  }
)

function removeScrollLock() {
  document.body.classList.remove('no-scroll')
  if (document.body.classList.length === 0) {
    document.body.removeAttribute('class')
  }
}

onUnmounted(() => {
  removeScrollLock()
})
</script>

<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal">
      <h2>Game Over</h2>
      <p class="icon">
        {{ result === 'win' ? '🏆' : result === 'lose' ? '😐' : '🤝' }}
      </p>
      <p class="winner-text">{{ winner }}</p>
      <div class="buttons">
        <button @click="handleRestart">Restart</button>
        <button class="destructive" @click="goHome">Exit</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal {
  background-color: #fff;
  padding: 2rem 2.5rem;
  border-radius: 12px;
  text-align: center;
  min-width: 280px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.25);
}

.winner-text {
  font-weight: bold;
  margin: 1rem 0;
  font-size: 1.2rem;
  color: #333;
}

.buttons {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
}

.modal .buttons button {
  flex: 1;
  width: 0;
}

.icon {
  font-size: 3rem;
  margin-bottom: 0.5rem;
  margin-top: 2rem;
}
</style>
