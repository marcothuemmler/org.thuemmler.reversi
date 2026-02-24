import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { type HistoryState, useRouter } from 'vue-router'
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
    difficulty: 'MEDIUM',
    currentPlayer: 'BLACK',
    preferredSide: 'BLACK',
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

    const blackWins = blackCount.value > whiteCount.value
    const whiteWins = whiteCount.value > blackCount.value

    if (blackWins && sessionConfig.value.preferredSide === 'BLACK') return 'You Win!'
    if (whiteWins && sessionConfig.value.preferredSide === 'WHITE') return 'You Win!'

    if (blackWins || whiteWins) return 'You Lose!'

    return "It's a Draw!"
  })

  const result = computed<'win' | 'lose' | 'draw' | null>(() => {
    if (!isFinished.value) return null

    if (blackCount.value === whiteCount.value) return 'draw'

    const youWon =
      (blackCount.value > whiteCount.value && sessionConfig.value.preferredSide === 'BLACK') ||
      (whiteCount.value > blackCount.value && sessionConfig.value.preferredSide === 'WHITE')

    return youWon ? 'win' : 'lose'
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

  function onMessage(event: MessageEvent) {
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
    result,
  }
}
