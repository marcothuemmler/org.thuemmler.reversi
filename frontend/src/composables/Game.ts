import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { useRouter, type HistoryState } from 'vue-router'
import clickSound from '../assets/click.mp3'

type PlayerType = 'HUMAN' | 'AI'
type Player = 'BLACK' | 'WHITE'
type CellState = Player | 'EMPTY'

interface Move {
  row: number
  col: number
}

interface GameState {
  id: string
  currentPlayer: Player
  isFinished: boolean
  board: { grid: CellState[][] }
  validMoves: Move[]
}

interface ServerMessage<T> {
  type: string
  gameId?: string
  payload: T
}

interface PlayerTypes {
  BLACK: PlayerType
  WHITE: PlayerType
}

interface NewGameRequest {
  id?: string
  playerTypes: PlayerTypes
  currentPlayer: Player
}

export function useGame() {
  const centerOffset = 'translate(-50%, -50%)'
  const gridSize = 8
  const socket = ref<WebSocket | null>(null)
  const gameState = ref<GameState | null>(null)
  const currentPlayer = ref<Player | null>(null)
  const validMoves = ref<Move[]>([])
  const highlightedCells = ref<Set<string>>(new Set())
  const isFinished = ref(false)
  const sessionConfig = ref<NewGameRequest>({
    playerTypes: { BLACK: 'HUMAN', WHITE: 'AI' },
    currentPlayer: 'BLACK',
  })
  const clickAudio = new Audio(clickSound)
  clickAudio.preload = 'auto'
  const router = useRouter()

  const board = reactive<CellState[][]>(
    Array.from({ length: gridSize }, () => Array.from({ length: gridSize }, () => 'EMPTY')),
  )

  const blackCount = computed(() =>
    board.reduce((acc, row) => acc + row.filter((cell) => cell === 'BLACK').length, 0),
  )

  const whiteCount = computed(() =>
    board.reduce((acc, row) => acc + row.filter((cell) => cell === 'WHITE').length, 0),
  )

  onMounted(() => {
    const state = window.history.state
    const query = router.currentRoute.value.query
    if (query.id) {
      sessionConfig.value.id = query.id as string
    }
    sessionConfig.value.playerTypes = playerTypesFromState(state)
    setupWebSocket(!query.id)
  })

  onUnmounted(() => socket.value?.close())

  function playSound() {
    const audio = clickAudio.cloneNode(true) as HTMLAudioElement
    audio.currentTime = 0
    audio.play().catch(console.error)
  }

  async function createGame(newGame?: NewGameRequest) {
    const payload = newGame ?? sessionConfig.value
    socket.value?.send(JSON.stringify({ payload, type: 'CREATE' }))
  }

  async function joinGame(id?: string) {
    socket.value?.send(JSON.stringify({ gameId: id, type: 'JOIN' }))
  }

  function setupWebSocket(createOnEnter: boolean) {
    if (socket.value) return

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'

    socket.value = new WebSocket(`${protocol}://${window.location.host}/ws/games`)

    socket.value.onopen = () => {
      console.log('WebSocket connected')
      if (createOnEnter) {
        createGame()
      } else {
        joinGame(sessionConfig.value.id)
      }
    }
    socket.value.onerror = (err) => console.error('WebSocket error', err)
    socket.value.onclose = () => console.log('WebSocket closed')

    socket.value.onmessage = (event) => {
      try {
        const game = JSON.parse(event.data)
        const type = game.type
        switch (type) {
          case 'CREATE': {
            updateBoard(game)
            break
          }
          case 'MAKE_MOVE': {
            playSound()
            updateBoard(game)
            break
          }
          case 'JOIN': {
            updateBoard(game)
            break
          }
        }
      } catch (err) {
        console.error('Failed to parse WebSocket message', err)
      }
    }
  }

  function updateBoard(game: ServerMessage<GameState>) {
    gameState.value = game.payload
    currentPlayer.value = game.payload.currentPlayer
    isFinished.value = game.payload.isFinished
    validMoves.value = game.payload.validMoves
    sessionConfig.value.id = game.payload.id

    router.replace({ path: '/game', query: { id: game.payload.id } })

    renderBoard(game.payload.board.grid)
  }

  function toggleValidMoves() {
    validMoves.value.forEach(toggleCell)
  }

  function toggleCell(move: Move) {
    const moveString = `${move.row}-${move.col}`
    const set = new Set(highlightedCells.value)
    set.has(moveString) ? set.delete(moveString) : set.add(moveString)
    highlightedCells.value = set
  }

  function renderBoard(grid: CellState[][]) {
    grid.forEach((row, i) => row.forEach((cell, j) => (board[i][j] = cell)))
  }

  async function makeMove(row: number, col: number) {
    if (isFinished.value) {
      createGame()
    }

    const isValid = validMoves.value.some((m) => m.row === row && m.col === col)
    if (!isValid) return

    socket.value?.send(
      JSON.stringify({ gameId: gameState.value?.id, payload: { row, col }, type: 'MAKE_MOVE' }),
    )
    highlightedCells.value.clear()
  }

  function cellClass(i: number, j: number) {
    return { cell: true, highlight: highlightedCells.value.has(`${i}-${j}`) }
  }

  function pieceTransform(i: number, j: number) {
    const state = board[i][j]
    return state === 'WHITE' ? `${centerOffset} rotateY(180deg)` : `${centerOffset} rotateY(0deg)`
  }

  function playerTypesFromState(state: HistoryState): PlayerTypes {
    return state?.mode === 'Multiplayer'
      ? { BLACK: 'HUMAN', WHITE: 'HUMAN' }
      : { BLACK: 'HUMAN', WHITE: 'AI' }
  }

  function undoMove() {
    socket.value?.send(JSON.stringify({ gameId: gameState.value?.id, type: 'UNDO' }))
    highlightedCells.value.clear()
  }
  function redoMove() {
    socket.value?.send(JSON.stringify({ gameId: gameState.value?.id, type: 'REDO' }))
    highlightedCells.value.clear()
  }

  return {
    gridSize,
    board,
    blackCount,
    whiteCount,
    createGame,
    toggleValidMoves,
    undoMove,
    redoMove,
    makeMove,
    cellClass,
    pieceTransform,
  }
}
