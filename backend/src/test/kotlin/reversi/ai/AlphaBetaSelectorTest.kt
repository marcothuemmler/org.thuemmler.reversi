package reversi.ai

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reversi.model.Board
import reversi.model.CellState
import reversi.model.Game
import kotlin.test.assertNull

class AlphaBetaSelectorTest {

    private val gameService = mockk<reversi.service.GameService>(relaxed = true)
    private val selector = AlphaBetaSelector(gameService, baseDepth = 1)

    @Test
    fun `selectMove returns null if no valid moves`() {
        val game = Game(
            id = "game1",
            board = Board.new(2, CellState.EMPTY),
            currentPlayer = CellState.BLACK
        )

        every { gameService.getValidMoves(game.id) } returns emptyList()

        val move = selector.selectMove(game)
        assertNull(move)
    }

    @Test
    fun `selectMove returns a move from validMoves`() {
        val game = Game(
            id = "game2",
            board = Board.new(2, CellState.EMPTY),
            currentPlayer = CellState.BLACK
        )

        val validMoves = listOf(0 to 0, 0 to 1)
        every { gameService.getValidMoves(game.id) } returns validMoves
        every { gameService.getFlippableCells(any(), any(), any(), any(), any(), any()) } returns listOf()
        every { gameService.applyMove(any(), any(), any(), any(), any()) } answers { firstArg() }
        every { gameService.isGameFinished(any()) } returns false

        val move = selector.selectMove(game)
        assert(move in validMoves)
    }

    @Test
    fun `selectMove prefers higher weighted move`() {
        val game = Game(
            id = "game3",
            board = Board.new(8, CellState.EMPTY),
            currentPlayer = CellState.BLACK
        )

        val validMoves = listOf(0 to 0, 3 to 3)
        every { gameService.getValidMoves(game.id) } returns validMoves
        every { gameService.getFlippableCells(any(), any(), any(), any(), any(), any()) } returns listOf()
        every { gameService.applyMove(any(), any(), any(), any(), any()) } answers { firstArg() }
        every { gameService.isGameFinished(any()) } returns false

        val move = selector.selectMove(game)
        assertEquals(0 to 0, move)
    }

}
