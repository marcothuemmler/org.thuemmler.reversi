package reversi.util

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reversi.model.Game
import reversi.service.GameService
import reversi.websocket.dto.MessageType

class MoveCommandTest {

    private lateinit var gameService: GameService
    private lateinit var command: MoveCommand
    private val gameId = "game-1"
    private val row = 2
    private val col = 3
    private val mockGame = mockk<Game>(relaxed = true)

    @BeforeEach
    fun setup() {
        gameService = mockk(relaxed = true)
        every { gameService.getGame(gameId) } returns mockGame
        every { gameService.makeMove(gameId, row, col) } returns mockGame
        command = MoveCommand(gameService, gameId, row, col)
    }

    @Test
    fun `doStep stores previous and new game states`() {
        command.doStep()

        verifyOrder {
            gameService.getGame(gameId)
            gameService.makeMove(gameId, row, col)
        }
    }

    @Test
    fun `undoStep saves previous game state`() {
        command.undoStep()
        verify(exactly = 0) { gameService.saveState(any(), any()) }

        command.doStep()
        command.undoStep()
        verify { gameService.saveState(mockGame, MessageType.UNDO) }
    }

    @Test
    fun `redoStep saves new game state`() {
        command.redoStep()
        verify(exactly = 0) { gameService.saveState(any(), any()) }

        command.doStep()
        command.redoStep()
        verify { gameService.saveState(mockGame, MessageType.REDO) }
    }

    @Test
    fun `undo then redo calls correct methods in order`() {
        command.doStep()
        command.undoStep()
        command.redoStep()

        verifySequence {
            gameService.getGame(gameId)
            gameService.makeMove(gameId, row, col)
            gameService.saveState(mockGame, MessageType.UNDO)
            gameService.saveState(mockGame, MessageType.REDO)
        }
    }
}
