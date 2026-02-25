package reversi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reversi.ai.MoveSelectorStrategy
import reversi.controller.dto.NewGameRequest
import reversi.model.CellState
import reversi.model.Difficulty
import reversi.model.PlayerType
import reversi.store.GameStore
import reversi.websocket.dto.MessageType
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameServiceTest {

    private lateinit var store: GameStore
    private lateinit var moveSelector: MoveSelectorStrategy
    private lateinit var publisher: GameEventPublisher
    private lateinit var operations: MoveEngine
    private lateinit var service: GameServiceImpl

    @BeforeEach
    fun setUp() {
        store = GameStore()
        moveSelector = mockk(relaxed = true)
        publisher = mockk(relaxed = true)
        operations = MoveEngineImpl()
        service = GameServiceImpl(store, publisher, moveSelector, operations)
    }

    @Test
    fun `createGame should initialize game with human and AI correctly`() = runTest {
        val request = NewGameRequest(
            id = "game1",
            difficulty = Difficulty.MEDIUM,
            playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
            preferredSide = CellState.BLACK
        )

        val game = service.createGame(request)

        assertEquals("game1", game.id)
        assertEquals(CellState.BLACK, game.currentPlayer)
        assertTrue(game.validMoves.isNotEmpty())
        verify { publisher.notify(any(), MessageType.CREATE) }
    }

    @Test
    fun `listGames should return created games`() = runTest {
        service.createGame(
            NewGameRequest(
                id = "game1",
                difficulty = Difficulty.MEDIUM,
                playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN),
                preferredSide = CellState.BLACK
            )
        )

        val games = service.listGames()
        assertEquals(1, games.size)
        assertEquals("game1", games.first().id)
    }

    @Test
    fun `getGame should return saved game or null`() = runTest {
        service.createGame(
            NewGameRequest(
                id = "game1",
                difficulty = Difficulty.MEDIUM,
                playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN),
                preferredSide = CellState.BLACK
            )
        )

        val game = service.getGame("game1")
        assertNotNull(game)
        assertEquals("game1", game.id)

        val missing = service.getGame("unknown")
        assertEquals(null, missing)
    }

    @Test
    fun `removeGame should delete game from store`() = runTest {
        service.createGame(
            NewGameRequest(
                id = "game1",
                difficulty = Difficulty.MEDIUM,
                playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN),
                preferredSide = CellState.BLACK
            )
        )

        service.removeGame("game1")
        assertEquals(null, service.getGame("game1"))
    }

    @Test
    fun `saveState should store game and notify publisher`() = runTest {
        val game = service.createGame(
            NewGameRequest(
                id = "game1",
                difficulty = Difficulty.MEDIUM,
                playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN),
                preferredSide = CellState.BLACK
            )
        )

        val updatedGame = service.saveState(game, MessageType.MAKE_MOVE)
        assertEquals(game.id, updatedGame.id)
        verify { publisher.notify(any(), MessageType.MAKE_MOVE) }
    }

    @Test
    fun `makeMove should apply player move and AI move`() = runTest {
        val gameId = "game1"
        service.createGame(
            NewGameRequest(
                id = gameId,
                difficulty = Difficulty.MEDIUM,
                playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
                preferredSide = CellState.BLACK
            )
        )

        every { moveSelector.selectMove(any()) } returns Pair(2, 4)

        val resultGame = service.makeMove(gameId, 2, 3)

        assertEquals(CellState.BLACK, resultGame.currentPlayer)
        assertFalse(resultGame.isFinished)
    }
}
