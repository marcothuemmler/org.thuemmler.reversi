<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const selected = ref<'Singleplayer' | 'Multiplayer'>('Singleplayer')
const router = useRouter()

function submit() {
  if (!selected.value) {
    alert('Please select an option')
    return
  }

  router.push({
    path: '/game',
    state: { mode: selected.value, currentPlayer: 'BLACK' },
  })
}
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-50">
    <h1 class="text-3xl font-bold mb-8 text-gray-800">Choose Game Mode</h1>

    <div class="flex flex-col md:flex-row gap-6 mb-8">
      <label
        class="flex items-center p-4 cursor-pointer border rounded-lg transition-all duration-200 hover:shadow-md"
        :class="{
          'border-blue-500 bg-blue-50': selected === 'Singleplayer',
          'border-gray-300 bg-white': selected !== 'Singleplayer'
        }"
      >
        <input
          type="radio"
          value="Singleplayer"
          v-model="selected"
          class="hidden"
        />
        <span class="ml-2 text-lg font-medium text-gray-700">Singleplayer</span>
      </label>

      <label
        class="flex items-center p-4 cursor-pointer border rounded-lg transition-all duration-200 hover:shadow-md"
        :class="{
          'border-blue-500 bg-blue-50': selected === 'Multiplayer',
          'border-gray-300 bg-white': selected !== 'Multiplayer'
        }"
      >
        <input
          type="radio"
          value="Multiplayer"
          v-model="selected"
          class="hidden"
        />
        <span class="ml-2 text-lg font-medium text-gray-700">Multiplayer</span>
      </label>
    </div>

    <button
      type="button"
      @click="submit"
      class="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold shadow hover:bg-blue-700 transition-colors"
    >
      Start Game
    </button>
  </div>
</template>

<style scoped>
input[type="radio"]:focus + span {
  outline: 2px solid #3b82f6;
  outline-offset: 2px;
}
</style>
