import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'

interface Move {
  row: number
  col: number
}

interface GameState {
  currentPlayer: string
  isFinished: boolean
  board: { grid: string[][] }
  validMoves: Move[]
}

export function useGame() {
  const gridSize = 8
  const socket = ref<WebSocket | null>(null)

  const gameId = ref<string | null>(null)
  const currentPlayer = ref<string | null>(null)
  const validMoves = ref<string[]>([])
  const highlightedCells = ref<Set<string>>(new Set())
  const isFinished = ref(false)

  const board = reactive<string[][]>(
    Array.from({ length: gridSize }, () => Array.from({ length: gridSize }, () => 'empty')),
  )

  const blackCount = computed(() =>
    board.reduce((acc, row) => acc + row.filter((cell) => cell === 'black').length, 0),
  )

  const whiteCount = computed(() =>
    board.reduce((acc, row) => acc + row.filter((cell) => cell === 'white').length, 0),
  )

  const centerOffset = 'translate(-50%, -50%)'

  function playSound() {
    const audio = document.getElementById('click-sound') as HTMLAudioElement
    if (audio) {
      new Audio(audio.src).play()
    }
  }

  function normalizeGrid(grid: string[][]) {
    return grid.map((row) => row.map((cell) => cell.toLowerCase()))
  }

  async function createGame() {
    const res = await fetch('/games', { method: 'POST' })
    const game = await res.json()

    gameId.value = game.id
    currentPlayer.value = game.currentPlayer
    isFinished.value = false

    renderBoard(game.board.grid, true)
    await fetchValidMoves()
    highlightedCells.value.clear()
    setupWebSocket()
  }

  async function fetchValidMoves() {
    if (!gameId.value) return
    const res = await fetch(`/games/${gameId.value}/moves`)
    const moves: Move[] = await res.json()
    validMoves.value = moves.map((m: Move) => `${m.row}-${m.col}`)
  }

  function setupWebSocket() {
    if (!gameId.value) return

    if (socket.value?.readyState === WebSocket.OPEN) {
      socket.value.close()
    }

    socket.value = new WebSocket(`ws://${window.location.host}/ws/games?gameId=${gameId.value}`)

    socket.value.onopen = () => console.log('WebSocket connected')
    socket.value.onerror = (err) => console.error('WebSocket error', err)
    socket.value.onclose = () => console.log('WebSocket closed')

    socket.value.onmessage = (event) => {
      playSound()
      try {
        const game: GameState = JSON.parse(event.data)
        currentPlayer.value = game.currentPlayer
        isFinished.value = game.isFinished
        validMoves.value = game.validMoves?.map(m => `${m.row}-${m.col}`) ?? []
        renderBoard(game.board.grid)
      } catch (err) {
        console.error('Failed to parse WebSocket message', err)
      }
    }
  }

  function highlightCells() {
    validMoves.value.forEach(toggleCell)
  }

  function toggleCell(move: string) {
    highlightedCells.value.has(move) ? highlightedCells.value.delete(move) : highlightedCells.value.add(move)
  }

  function renderBoard(grid: string[][], init = false) {
    const normalized = normalizeGrid(grid)
    normalized.forEach((row, i) => row.forEach((cell, j) => (board[i][j] = cell)))
  }

  async function makeMove(row: number, col: number) {
    const key = `${row}-${col}`
    if (isFinished.value) {
      await createGame()
    }
    if (!validMoves.value.includes(key)) {
      return
    }

    socket.value?.send(JSON.stringify({ row, col }))
    highlightedCells.value.clear()
  }

  async function undoMove() {
    await updateBoard('/undo')
    return
  }

  async function redoMove() {
    await updateBoard('/redo')
  }

  async function updateBoard(endpoint: string) {
    if (!gameId.value) return
    const res = await fetch(`/games/${gameId.value}${endpoint}`, { method: 'POST' })
    if (!res.ok) return
    const game = await res.json()

    isFinished.value = game.isFinished
    currentPlayer.value = game.currentPlayer
    renderBoard(game.board.grid)
    await fetchValidMoves()
  }

  function cellClass(i: number, j: number) {
    return { cell: true, highlight: highlightedCells.value.has(`${i}-${j}`) }
  }

  function pieceTransform(i: number, j: number) {
    const state = board[i][j]
    if (state === 'empty') return `${centerOffset} rotateY(0deg)`
    return state === 'black' ? `${centerOffset} rotateY(0deg)` : `${centerOffset} rotateY(180deg)`
  }

  onMounted(createGame)
  onUnmounted(() => socket.value?.close())

  return {
    gridSize,
    board,
    blackCount,
    whiteCount,
    createGame,
    highlightCells,
    undoMove,
    redoMove,
    makeMove,
    cellClass,
    pieceTransform,
  }
}
