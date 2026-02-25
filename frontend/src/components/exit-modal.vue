<script setup lang="ts">
defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'yes'): void
  (e: 'no'): void
}>()
</script>

<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal">
      <h2>Exit Game?</h2>
      <div class="buttons">
        <button @click="emit('no')">No</button>
        <button class="destructive" @click="emit('yes')">Yes</button>
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
</style>
