package reversi.controller

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reversi.controller.dto.MoveRequest
import reversi.controller.dto.NewGameRequest
import reversi.model.Board
import reversi.model.CellState
import reversi.model.Game
import reversi.model.PlayerType
import reversi.service.GameService
import reversi.util.UndoManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class GameControllerTest {

    private val service = mockk<GameService>(relaxed = true)
    private lateinit var undoManagers: MutableMap<String, UndoManager>
    private lateinit var controller: GameController

    private val sampleGame = Game(
        id = "game1", board = Board.new(2, CellState.EMPTY), currentPlayer = CellState.BLACK
    )

    @BeforeEach
    fun setup() {
        undoManagers = mutableMapOf()
        controller = GameController(service, undoManagers)
    }

    @Test
    fun `createGame delegates to service`() {
        val req =
            NewGameRequest(
                "player1",
                mapOf(CellState.BLACK to PlayerType.HUMAN, CellState.WHITE to PlayerType.HUMAN),
                CellState.BLACK,
                CellState.BLACK
            )
        every { service.createGame(req) } returns sampleGame

        val result = controller.createGame(req)

        assertSame(sampleGame, result)
        verify { service.createGame(req) }
    }

    @Test
    fun `listGames delegates to service`() {
        every { service.listGames() } returns listOf(sampleGame)

        val result = controller.listGames()

        assertEquals(1, result.size)
        assertSame(sampleGame, result[0])
    }

    @Test
    fun `getGame returns game when found`() {
        every { service.getGame("game1") } returns sampleGame

        val result = controller.getGame("game1")

        assertSame(sampleGame, result)
    }

    @Test
    fun `getGame throws when not found`() {
        every { service.getGame("missing") } returns null

        assertThrows<NoSuchElementException> {
            controller.getGame("missing")
        }
    }

    @Test
    fun `removeGame removes from undoManagers and calls service`() {
        undoManagers["game1"] = UndoManager()

        controller.removeGame("game1")

        assert(!undoManagers.containsKey("game1"))
        verify { service.removeGame("game1") }
    }

    @Test
    fun `makeMove creates UndoManager if absent`() {
        every { service.getGame("game1") } returns sampleGame

        val req = MoveRequest(0, 0)
        val result = controller.makeMove("game1", req)

        assertNotNull(undoManagers["game1"])
        assertSame(sampleGame, result)
    }

    @Test
    fun `undoMove returns latest game when manager exists`() {
        val manager = spyk(UndoManager())
        undoManagers["game1"] = manager

        val expected = Game(
            id = "game1",
            board = Board.new(CellState.EMPTY),
            currentPlayer = CellState.BLACK
        )
        every { service.getGame("game1") } returns expected

        val result = controller.undoMove("game1")

        assertSame(expected, result)
        verify(exactly = 1) { manager.undoStep() }
    }

    @Test
    fun `redoMove returns latest game when manager exists`() {
        val manager = spyk(UndoManager())
        undoManagers["game1"] = manager

        val expected = Game(
            id = "game1",
            board = Board.new(CellState.EMPTY),
            currentPlayer = CellState.BLACK
        )
        every { service.getGame("game1") } returns expected

        val result = controller.redoMove("game1")

        assertSame(expected, result)
        verify(exactly = 1) { manager.redoStep() }
    }

    @Test
    fun `undoMove throws if no undoManager`() {
        assertThrows<IllegalStateException> {
            controller.undoMove("game1")
        }
    }

    @Test
    fun `redoMove throws if no undoManager`() {
        assertThrows<IllegalStateException> {
            controller.redoMove("game1")
        }
    }
}
