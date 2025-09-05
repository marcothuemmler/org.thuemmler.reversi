package reversi.websocket

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import reversi.controller.dto.MoveRequest
import reversi.controller.dto.NewGameRequest
import reversi.model.Board
import reversi.model.CellState
import reversi.model.Game
import reversi.model.PlayerType
import reversi.service.GameService
import reversi.util.UndoManager
import reversi.websocket.dto.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertTrue

@kotlinx.coroutines.ExperimentalCoroutinesApi
class GameWebSocketHandlerTest {

    private val testScope = TestScope()
    private lateinit var gameService: GameService
    private lateinit var undoManagers: ConcurrentHashMap<String, UndoManager>
    private lateinit var sessions: SessionRegistry
    private lateinit var handler: GameWebSocketHandler
    private lateinit var webSocketSession: WebSocketSession

    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    @BeforeEach
    fun setUp() {
        gameService = mockk(relaxed = true)
        undoManagers = ConcurrentHashMap()
        sessions = mockk(relaxed = true)
        handler = GameWebSocketHandler(gameService, undoManagers, sessions, testScope)
        webSocketSession = mockk(relaxed = true)
        every { webSocketSession.id } returns "session1"
    }

    @Test
    fun `handleTextMessage should create game`() {
        val gameId = "game1"
        val newGameRequest = NewGameRequest(
            id = gameId, playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN), preferredSide = CellState.BLACK
        )
        val message = CreateGame(
            payload = newGameRequest
        )

        handler.handleMessage(webSocketSession, TextMessage(json.encodeToString(message)))

        testScope.advanceUntilIdle()
        coVerify { gameService.createGame(match { it.playerTypes == newGameRequest.playerTypes }) }
        verify { sessions.register(webSocketSession, any(), CellState.BLACK) }
        assertTrue(undoManagers.containsKey(gameId))
    }

    @Test
    fun `handleTextMessage should join game`() {
        val gameId = "game1"
        val game = Game(
            id = gameId,
            board = Board.new(CellState.EMPTY),
            playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN),
            currentPlayer = CellState.BLACK
        )
        every { gameService.getGame(gameId) } returns game
        every { sessions.openSessionsForGame(gameId) } returns emptySet()
        every { sessions.assignedSide(webSocketSession) } returns null

        val message = Join(gameId = gameId)

        handler.handleMessage(webSocketSession, TextMessage(json.encodeToString(message)))
        testScope.advanceUntilIdle()
        verify { sessions.register(webSocketSession, gameId, CellState.BLACK) }
        verify { webSocketSession.sendMessage(any()) }
    }

    @Test
    fun `handleTextMessage should reject move if out of turn`() {
        val gameId = "game1"
        val game = Game(
            id = gameId,
            board = Board.new(CellState.EMPTY),
            playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN),
            currentPlayer = CellState.BLACK
        )
        every { gameService.getGame(gameId) } returns game
        every { sessions.assignedSide(webSocketSession) } returns CellState.WHITE

        val moveRequest = MoveRequest(row = 0, col = 0)
        val message = MakeMove(
            gameId = gameId, payload = moveRequest
        )

        handler.handleMessage(webSocketSession, TextMessage(json.encodeToString(message)))

        testScope.advanceUntilIdle()
        verify { webSocketSession.sendMessage(match { it.payload.toString().contains("not your turn") }) }
    }

    @Test
    fun `undoMove should call UndoManager undoStep`() {
        val gameId = "game1"
        val undoManager = mockk<UndoManager>(relaxed = true)
        undoManagers[gameId] = undoManager

        handler.handleMessage(
            webSocketSession, TextMessage(json.encodeToString(Undo(gameId = gameId)))
        )
        testScope.advanceUntilIdle()

        coVerify { undoManager.undoStep() }
    }

    @Test
    fun `redoMove should call UndoManager redoStep`() {
        val gameId = "game1"
        val undoManager = mockk<UndoManager>(relaxed = true)
        undoManagers[gameId] = undoManager

        handler.handleMessage(
            webSocketSession, TextMessage(
                json.encodeToString(Redo(gameId = gameId))
            )
        )
        testScope.advanceUntilIdle()

        verify { undoManager.redoStep() }
    }

    @Test
    fun `afterConnectionClosed should remove undoManager and game when last session closes`() {
        val gameId = "game1"
        val undoManager = UndoManager()
        undoManagers[gameId] = undoManager

        every { sessions.remove(webSocketSession) } returns gameId
        every { sessions.hasOpenSessions(gameId) } returns false

        handler.afterConnectionClosed(webSocketSession, CloseStatus.NORMAL)

        assertFalse(undoManagers.containsKey(gameId))
        verify { gameService.removeGame(gameId) }
    }


    @Test
    fun `afterConnectionClosed should not remove game if other sessions exist`() {
        val gameId = "game1"
        undoManagers[gameId] = UndoManager()
        every { sessions.remove(webSocketSession) } returns gameId
        every { sessions.hasOpenSessions(gameId) } returns true

        handler.afterConnectionClosed(webSocketSession, CloseStatus.NORMAL)

        assertTrue(undoManagers.containsKey(gameId))
        verify(exactly = 0) { gameService.removeGame(gameId) }
    }

    @Test
    fun `making a move should broadcast to all open sessions`() {
        val gameId = "game1"

        val gameServiceMock = mockk<GameService>(relaxed = true)
        val subscribers = mutableListOf<(Game, MessageType) -> Unit>()
        every { gameServiceMock.subscribe(any()) } answers {
            subscribers += firstArg<(Game, MessageType) -> Unit>()
        }

        val undoManagers = ConcurrentHashMap<String, UndoManager>()
        val sessions = mockk<SessionRegistry>(relaxed = true)

        GameWebSocketHandler(gameServiceMock, undoManagers, sessions, testScope)

        val session1 = mockk<WebSocketSession>(relaxed = true)
        val session2 = mockk<WebSocketSession>(relaxed = true)
        every { sessions.forEachOpenSession(gameId, any()) } answers {
            val block = arg<(WebSocketSession) -> Unit>(1)
            block(session1)
            block(session2)
        }

        val game = Game(
            id = gameId,
            board = Board.new(CellState.EMPTY),
            playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.HUMAN),
            currentPlayer = CellState.BLACK
        )

        subscribers.forEach { it(game, MessageType.MAKE_MOVE) }
        testScope.advanceUntilIdle()
        coVerify { session1.sendMessage(any()) }
        coVerify { session2.sendMessage(any()) }
    }
}
