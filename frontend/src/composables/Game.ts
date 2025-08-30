import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { useRouter, type HistoryState } from 'vue-router'
import clickSound from '../assets/click.mp3'
import type { CellState, GameState, Move, NewGameRequest, Player, PlayerTypes } from '@/types/game'
import { ClientMessage, type ServerMessage } from '@/types/server'
import { useWebSocket } from './WebSocket'
import { getWebSocketUrl } from '@/utils/websocket'

export function useGame() {
  const centerOffset = 'translate(-50%, -50%)'
  const gridSize = 8
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

  const winner = computed(() => {
    if (!isFinished.value) return ''
    if (blackCount.value > whiteCount.value) return 'Black Wins!'
    if (whiteCount.value > blackCount.value) return 'White Wins!'
    return "It's a Draw!"
  })

  const socket = useWebSocket(getWebSocketUrl('games'), onMessage, onOpen)

  onMounted(() => {
    const state = window.history.state
    const query = router.currentRoute.value.query
    if (query.id) {
      sessionConfig.value.id = query.id as string
    }
    sessionConfig.value.playerTypes = playerTypesFromState(state)

    socket.connect(!query.id)
  })

  onUnmounted(() => socket?.close())

  function playSound() {
    const audio = clickAudio.cloneNode(true) as HTMLAudioElement
    audio.currentTime = 0
    audio.play().catch(console.error)
  }

  function createGame(newGame?: NewGameRequest) {
    const payload = newGame ?? sessionConfig.value
    socket.send(ClientMessage.create('CREATE').withPayload(payload).asString())
  }

  function joinGame(id?: string) {
    socket.send(ClientMessage.create('JOIN').withGameId(id).asString())
  }

  function onMessage(event: MessageEvent<string>) {
    try {
      const message: ServerMessage<GameState> = JSON.parse(event.data)
      const type = message.type
      switch (type) {
        case 'CREATE': {
          router.replace({ path: '/game', query: { id: message.payload.id } })
          updateBoard(message)
          break
        }
        case 'MAKE_MOVE': {
          playSound()
          updateBoard(message)
          break
        }
        case 'JOIN':
        case 'UNDO':
        case 'REDO': {
          updateBoard(message)
          break
        }
      }
    } catch (err) {
      console.error('Failed to parse WebSocket message', err)
    }
  }

  function onOpen(createOnEnter?: boolean) {
    if (createOnEnter) {
      createGame()
    } else {
      joinGame(sessionConfig.value.id)
    }
  }

  function updateBoard(message: ServerMessage<GameState>) {
    highlightedCells.value.clear()
    gameState.value = message.payload
    currentPlayer.value = message.payload.currentPlayer
    isFinished.value = message.payload.isFinished
    validMoves.value = message.payload.validMoves
    sessionConfig.value.id = message.payload.id

    renderBoard(message.payload.board.grid)
  }

  function toggleValidMoves() {
    validMoves.value.forEach(toggleCell)
  }

  function toggleCell(move: Move) {
    const moveString = `${move.row}-${move.col}`
    if (highlightedCells.value.has(moveString)) {
      highlightedCells.value.delete(moveString)
    } else {
      highlightedCells.value.add(moveString)
    }
  }

  function renderBoard(grid: CellState[][]) {
    grid.forEach((row, i) => row.forEach((cell, j) => (board[i][j] = cell)))
  }

  function makeMove(row: number, col: number) {
    const isValid = validMoves.value.some((m) => m.row === row && m.col === col)
    if (!isValid) return

    const move = { row, col }
    const id = gameState.value?.id
    const message = ClientMessage.create('MAKE_MOVE').withPayload(move).withGameId(id).asString()
    socket.send(message)
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
    socket.send(ClientMessage.create('UNDO').withGameId(gameState.value?.id).asString())
  }

  function redoMove() {
    socket.send(ClientMessage.create('REDO').withGameId(gameState.value?.id).asString())
  }

  return {
    gridSize,
    board,
    blackCount,
    whiteCount,
    isFinished,
    winner,
    createGame,
    toggleValidMoves,
    undoMove,
    redoMove,
    makeMove,
    cellClass,
    pieceTransform,
  }
}
