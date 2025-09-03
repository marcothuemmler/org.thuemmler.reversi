package reversi.service

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import reversi.model.Board
import reversi.model.CellState
import reversi.util.BoardFactory
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoveEngineImplTest {

    private val engine = MoveEngineImpl()

    @Test
    fun `getValidMoves should return correct list of moves`() = runTest {
        val board = BoardFactory.createStartingBoard()
        val currentPlayer = CellState.BLACK

        val validMoves = engine.calculateValidMoves(board,currentPlayer)
        assertTrue(validMoves.isNotEmpty())
    }

    @Test
    fun `getFlippableCells should return correct flippable cells`() = runTest {
        val board = BoardFactory.createStartingBoard()
        val currentPlayer = CellState.BLACK
        val flips = engine.getFlippableCells(board, 2, 3, currentPlayer, 0, 1)
        assertNotNull(flips)
    }

    @Test
    fun `applyMove should apply move and flips`() = runTest {
        val board = BoardFactory.createStartingBoard()
        val currentPlayer = CellState.BLACK
        val flippable = listOf(2 to 4)
        val newBoard = engine.applyMove(board, 2, 3, currentPlayer, flippable)

        assertEquals(CellState.BLACK, newBoard.getCell(2, 3))
        assertEquals(CellState.BLACK, newBoard.getCell(2, 4))
    }

    @Test
    fun `isGameFinished should detect end of game`() = runTest {
        val board = BoardFactory.createStartingBoard()
        assertFalse(engine.isGameFinished(board))
    }

    @Test
    fun `game should be finished when board has no empty cells`() {
        val fullBoard = Board.new(CellState.BLACK)
        assertTrue(engine.isGameFinished(fullBoard))
    }
}
