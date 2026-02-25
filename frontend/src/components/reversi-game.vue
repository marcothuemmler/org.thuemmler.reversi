<script setup lang="ts">
import { ref, watch } from 'vue'
import { useGame } from '@/composables/Game'
import GameOverModal from '@/components/game-over-modal.vue'
import ExitModal from '@/components/exit-modal.vue'
import 'primeicons/primeicons.css'

const {
  isFinished,
  createGame,
  blackCount,
  whiteCount,
  gridSize,
  board,
  cellClass,
  goHome,
  makeMove,
  pieceTransform,
  toggleValidMoves,
  undoMove,
  redoMove,
  result,
  winner,
} = useGame()
const showModal = ref(false)
const showExitModal = ref(false)

function requestExit() {
  showExitModal.value = true
}

watch(
  () => isFinished.value,
  (finished) => {
    if (finished) setTimeout(() => (showModal.value = true), 500)
  },
)

function restartGame() {
  showModal.value = false
  createGame()
}

function closeExitModal() {
  showExitModal.value = false
}
</script>

<template>
  <div class="reversi-game">
    <div class="exit-button" @click="showExitModal = true">
      <i class="pi pi-times exit-icon" />
    </div>
    <p class="score">Black: {{ blackCount }} | White: {{ whiteCount }}</p>
    <div id="board" :style="{ gridTemplateColumns: `repeat(${gridSize}, 1fr)` }">
      <div
        v-for="(cell, index) in board.flat()"
        :key="index"
        :class="cellClass(Math.floor(index / gridSize), index % gridSize)"
        @click="makeMove(Math.floor(index / gridSize), index % gridSize)"
      >
        <div
          class="piece"
          :style="{
            visibility: cell === 'EMPTY' ? 'hidden' : 'visible',
            transform: pieceTransform(Math.floor(index / gridSize), index % gridSize),
            transition: 'transform 0.5s',
          }"
        >
          <div class="front"></div>
          <div class="back"></div>
        </div>
      </div>
    </div>
    <div class="controls">
      <button @click="restartGame"><i class="pi pi-angle-double-left button-icon" />Restart</button>
      <button @click="toggleValidMoves"><i class="pi pi-star-fill button-icon" />Hint</button>
      <button @click="undoMove"><i class="pi pi-undo button-icon" />Undo</button>
      <button @click="redoMove"><i class="pi pi-refresh button-icon" /> Redo</button>
    </div>
    <GameOverModal :show="showModal" :winner="winner" @restart="restartGame" :result="result" @exit="requestExit" />
    <ExitModal :show="showExitModal" @yes="goHome" @no="closeExitModal" />
  </div>
</template>

<style scoped src="@/styles/reversi-game.css"></style>
