<script setup lang="ts">
import { ref, watch } from 'vue'
import { useGame } from '@/composables/Game'
import GameOverModal from './GameOverModal.vue'

const {
  isFinished,
  createGame,
  blackCount,
  whiteCount,
  gridSize,
  board,
  cellClass,
  makeMove,
  pieceTransform,
  toggleValidMoves,
  undoMove,
  redoMove,
  winner,
} = useGame()
const showModal = ref(false)

watch(
  () => isFinished.value,
  (finished) => {
    if (finished) setTimeout(() => (showModal.value = true), 500)
  }
)

function restartGame() {
  showModal.value = false
  createGame()
}
</script>

<template>
  <div id="game">
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
      <button @click="restartGame">Restart Game</button>
      <button @click="toggleValidMoves">Hint</button>
      <button @click="undoMove">Undo</button>
      <button @click="redoMove">Redo</button>
    </div>
    <GameOverModal :show="showModal" :winner="winner" @restart="restartGame" />
  </div>
</template>

<style scoped src="@/styles/Game.css"></style>
