<script setup lang="ts">
import { useGame } from '../composables/Game'

const game = useGame()
</script>

<template>
  <div id="game">
    <h1>Reversi</h1>
    <button @click="game.createGame">New Game</button>
    <button @click="game.highlightCells">Highlight Valid Moves</button>
    <button @click="game.undoMove">Undo</button>
    <button @click="game.redoMove">Redo</button>
    <p>Black: {{ game.blackCount }} | White: {{ game.whiteCount }}</p>
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
            visibility: cell === 'empty' ? 'hidden' : 'visible',
            transform: game.pieceTransform(Math.floor(index / game.gridSize), index % game.gridSize),
            transition: 'transform 0.5s',
          }"
        >
          <div class="front"></div>
          <div class="back"></div>
        </div>
      </div>
    </div>
    <audio
      id="click-sound"
      src="https://cdn.freesound.org/previews/157/157776_1138047-lq.mp3"
      preload="auto"
    ></audio>
  </div>
</template>

<style src="../styles/Game.css"></style>
