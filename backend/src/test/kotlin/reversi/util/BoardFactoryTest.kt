package reversi.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reversi.model.CellState
import kotlin.test.assertEquals

class BoardFactoryTest {

    @Test
    fun `default board is 8x8 with correct initial pieces`() {
        val board = BoardFactory.createStartingBoard()
        assertEquals(8, board.size, "Board should be 8x8 by default")

        val mid = board.size / 2
        assertEquals(CellState.WHITE, board.getCell(mid - 1, mid - 1))
        assertEquals(CellState.WHITE, board.getCell(mid, mid))
        assertEquals(CellState.BLACK, board.getCell(mid - 1, mid))
        assertEquals(CellState.BLACK, board.getCell(mid, mid - 1))

        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                if ((row == mid - 1 && col == mid - 1) || (row == mid && col == mid)
                    || (row == mid - 1 && col == mid) || (row == mid && col == mid - 1)
                ) continue
                assertEquals(CellState.EMPTY, board.getCell(row, col))
            }
        }
    }

    @Test
    fun `custom even-sized board has correct dimensions and setup`() {
        val size = 10
        val board = BoardFactory.createStartingBoard(size)
        assertEquals(size, board.size, "Board size should match requested size")

        val mid = size / 2
        assertEquals(CellState.WHITE, board.getCell(mid - 1, mid - 1))
        assertEquals(CellState.WHITE, board.getCell(mid, mid))
        assertEquals(CellState.BLACK, board.getCell(mid - 1, mid))
        assertEquals(CellState.BLACK, board.getCell(mid, mid - 1))
    }

    @Test
    fun `creating board with odd size throws exception`() {
        assertThrows<IllegalArgumentException> {
            BoardFactory.createStartingBoard(5)
        }
    }
}
