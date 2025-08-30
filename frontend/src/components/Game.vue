<script setup lang="ts">
import { ref, watch } from 'vue'
import { useGame } from '@/composables/Game'
import GameOverModal from './GameOverModal.vue'

const game = useGame()
const showModal = ref(false)

watch(
  () => game.isFinished.value,
  (finished) => {
    if (finished) setTimeout(() => (showModal.value = true), 500)
  }
)

function restartGame() {
  showModal.value = false
  game.createGame()
}
</script>

<template>
  <div id="game">
    <p class="score">Black: {{ game.blackCount }} | White: {{ game.whiteCount }}</p>
    <div id="board" :style="{ gridTemplateColumns: `repeat(${game.gridSize}, 50px)` }">
      <div
        v-for="(cell, index) in game.board.flat()"
        :key="index"
        :class="game.cellClass(Math.floor(index / game.gridSize), index % game.gridSize)"
        @click="game.makeMove(Math.floor(index / game.gridSize), index % game.gridSize)"
      >
        <div
          class="piece"
          :style="{
            visibility: cell === 'EMPTY' ? 'hidden' : 'visible',
            transform: game.pieceTransform(
              Math.floor(index / game.gridSize),
              index % game.gridSize
            ),
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
      <button @click="game.toggleValidMoves">Hint</button>
      <button @click="game.undoMove">Undo</button>
      <button @click="game.redoMove">Redo</button>
    </div>
    <GameOverModal :show="showModal" :winner="game.winner.value" @restart="restartGame" />
  </div>
</template>

<style scoped src="../styles/Game.css"></style>
