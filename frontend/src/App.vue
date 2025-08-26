<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'

const gridSize = 8

const gameId = ref<string | null>(null)
const currentPlayer = ref<string | null>(null)
const validMoves = ref<string[]>([])
const highlightedCells = ref<Set<string>>(new Set())
const previousGrid = ref<string[][] | null>(null)
const isFinished = ref(false)
const blackCount = ref(2)
const whiteCount = ref(2)

const board = reactive<string[][]>(
  Array.from({ length: gridSize }, () => Array.from({ length: gridSize }, () => 'empty'))
)

function playSound() {
  const audio = document.getElementById('click-sound') as HTMLAudioElement
  if (audio) {
    const sound = new Audio(audio.src)
    sound.play()
  }
}

async function createGame() {
  const res = await fetch('/games', { method: 'POST' })
  const game = await res.json()

  gameId.value = game.id
  currentPlayer.value = game.currentPlayer
  previousGrid.value = null
  isFinished.value = false

  renderBoard(game.board.grid, true)
  await fetchValidMoves()
  highlightedCells.value.clear()
}

async function fetchValidMoves() {
  if (!gameId.value) return
  const res = await fetch(`/games/${gameId.value}/moves`)
  const moves = await res.json()
  validMoves.value = moves.map((m: any) => `${m.row}-${m.col}`)
}

function highlightCells() {
  highlightedCells.value.clear()
  validMoves.value.forEach((move) => highlightedCells.value.add(move))
}

function renderBoard(grid: string[][], init = false) {
  for (let i = 0; i < gridSize; i++) {
    for (let j = 0; j < gridSize; j++) {
      board[i][j] = grid[i][j].toLowerCase()
    }
  }

  countPieces(grid)

  if (init || !previousGrid.value) {
    previousGrid.value = grid.map((row) => [...row])
    return
  }

  previousGrid.value = grid.map((row) => [...row])
}

function countPieces(grid: string[][]) {
  let black = 0
  let white = 0
  grid.forEach((row) => {
    row.forEach((cell) => {
      if (cell.toLowerCase() === 'black') black++
      else if (cell.toLowerCase() === 'white') white++
    })
  })
  blackCount.value = black
  whiteCount.value = white
}

async function makeMove(row: number, col: number) {
  const key = `${row}-${col}`
  if (isFinished.value) {
    await createGame()
    return
  }
  if (!validMoves.value.includes(key)) {
    return
  }

  const res = await fetch(`/games/${gameId.value}/moves`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ row, col }),
  })
  const game = await res.json()

  isFinished.value = game.isFinished
  currentPlayer.value = game.currentPlayer

  renderBoard(game.board.grid)
  await fetchValidMoves()
  playSound()
  highlightedCells.value.clear()
}

function cellClass(i: number, j: number) {
  const key = `${i}-${j}`
  return {
    cell: true,
    highlight: highlightedCells.value.has(key),
  }
}

const centerOffset = 'translate(-50%, -50%)'

function pieceTransform(i: number, j: number) {
  const newState = board[i][j]
  if (newState === 'empty') return `${centerOffset} rotateY(0deg)`
  return newState === 'black' ? `${centerOffset} rotateY(0deg)` : `${centerOffset} rotateY(180deg)`
}

onMounted(() => {
  createGame()
})
</script>

<template>
  <div id="game">
    <h1>Reversi</h1>
    <button @click="createGame">New Game</button>
    <button @click="highlightCells">Highlight Valid Moves</button>
    <p>Black: {{ blackCount }} | White: {{ whiteCount }}</p>
    <div id="board" :style="{ gridTemplateColumns: `repeat(${gridSize}, 50px)` }">
      <div
        v-for="(cell, index) in board.flat()"
        :key="index"
        :class="cellClass(Math.floor(index / gridSize), index % gridSize)"
        @click="makeMove(Math.floor(index / gridSize), index % gridSize)"
      >
        <div
          class="piece"
          :style="{
            visibility: cell === 'empty' ? 'hidden' : 'visible',
            transform: pieceTransform(Math.floor(index / gridSize), index % gridSize),
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

<style>
#game {
  display: flex;
  flex-direction: column;
  align-items: center;
}
</style>