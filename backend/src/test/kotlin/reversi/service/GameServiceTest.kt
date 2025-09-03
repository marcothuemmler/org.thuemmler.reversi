package reversi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reversi.ai.MoveSelectorStrategy
import reversi.controller.dto.NewGameRequest
import reversi.model.Board
import reversi.model.CellState
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
    private lateinit var service: GameService

    @BeforeEach
    fun setUp() {
        store = GameStore()
        moveSelector = mockk(relaxed = true)
        publisher = mockk(relaxed = true)
        service = GameService(store, publisher, moveSelector)
    }

    @Test
    fun `createGame should initialize game with human and AI correctly`() = runTest {
        val request = NewGameRequest(
            id = "game1",
            playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
            preferredSide = CellState.BLACK
        )

        every { moveSelector.selectMove(any()) } returns Pair(2, 4)

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
                playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
                preferredSide = CellState.BLACK
            )
        )

        every { moveSelector.selectMove(any()) } returns Pair(2, 4)

        val resultGame = service.makeMove(gameId, 2, 3)

        assertEquals(CellState.BLACK, resultGame.currentPlayer)
        assertFalse(resultGame.isFinished)
    }

    @Test
    fun `getValidMoves should return correct list of moves`() = runTest {
        val gameId = "game1"
        service.createGame(
            NewGameRequest(
                id = gameId,
                playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN),
                preferredSide = CellState.BLACK
            )
        )

        val validMoves = service.getValidMoves(gameId)
        assertTrue(validMoves.isNotEmpty())
    }

    @Test
    fun `getFlippableCells should return correct flippable cells`() = runTest {
        val game = service.createGame(
            NewGameRequest(
                id = "game1",
                playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
                preferredSide = CellState.BLACK
            )
        )

        val flips = service.getFlippableCells(game.board, 2, 3, CellState.BLACK, 0, 1)
        assertNotNull(flips)
    }

    @Test
    fun `applyMove should apply move and flips`() = runTest {
        val game = service.createGame(
            NewGameRequest(
                id = "game1",
                playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
                preferredSide = CellState.BLACK
            )
        )
        val flippable = listOf(2 to 4)
        val newBoard = service.applyMove(game.board, 2, 3, CellState.BLACK, flippable)

        assertEquals(CellState.BLACK, newBoard.getCell(2, 3))
        assertEquals(CellState.BLACK, newBoard.getCell(2, 4))
    }

    @Test
    fun `isGameFinished should detect end of game`() = runTest {
        val game = service.createGame(
            NewGameRequest(
                id = "game1",
                playerTypes = mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.AI),
                preferredSide = CellState.BLACK
            )
        )

        assertFalse(service.isGameFinished(game.board))
    }

    @Test
    fun `game should be finished when board has no empty cells`() {
        val fullBoard = Board.new(CellState.BLACK)
        assertTrue(service.isGameFinished(fullBoard))
    }
}
