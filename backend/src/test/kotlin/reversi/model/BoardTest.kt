package reversi.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BoardTest {

    @Test
    fun `new creates board with correct size and default value`() {
        val board = Board.new(4, CellState.EMPTY)
        assertEquals(4, board.size)
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                assertEquals(CellState.EMPTY, board.getCell(r, c))
            }
        }
    }

    @Test
    fun `new single parameter overload`() {
        val board = Board.new(0)
        assertEquals(8, board.size)
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                assertEquals(0, board.getCell(r, c))
            }
        }
    }


    @Test
    fun `setCell updates only the specified cell and returns new board`() {
        val board = Board.new(3, 0)
        val newBoard = board.setCell(1, 1, 5)

        assertEquals(0, board.getCell(1, 1))

        assertEquals(5, newBoard.getCell(1, 1))

        assertEquals(0, newBoard.getCell(0, 0))
        assertEquals(0, newBoard.getCell(2, 2))
    }

    @Test
    fun `copy method returns identical board except modified cell`() {
        val board = Board.new(2, 0)
        val copyBoard = board.copy(grid = listOf(listOf(1, 0), listOf(0, 0)))
        assertEquals(2, copyBoard.size)
        assertEquals(1, copyBoard.getCell(0, 0))
        assertEquals(0, copyBoard.getCell(1, 1))
    }

    @Test
    fun `serialization round trip`() {
        val board = Board.new(2, 0)
        val json = Json.encodeToString(board)
        val decoded = Json.decodeFromString<Board<Int>>(json)
        assertEquals (board, decoded)
    }
}
