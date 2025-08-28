<script setup lang="ts">
import { useGame } from '../composables/Game'

const game = useGame()
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
      <button @click="() => game.createGame()">Restart Game</button>
      <button @click="game.toggleValidMoves">Hint</button>
      <button @click="game.undoMove">Undo</button>
      <button @click="game.redoMove">Redo</button>
    </div>
  </div>
</template>

<style scoped src="../styles/Game.css"></style>
