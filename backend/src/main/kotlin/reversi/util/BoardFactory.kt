package reversi.util

import reversi.model.Board
import reversi.model.CellState
import reversi.model.GameBoard

object BoardFactory {

    fun createStartingBoard(size: Int = 8): GameBoard {
        require(size % 2 == 0) { "Board size must be even" }

        val mid = size / 2
        val grid = List(size) { row ->
            List(size) { col ->
                when {
                    (row == mid - 1 && col == mid - 1) || (row == mid && col == mid) -> CellState.WHITE
                    (row == mid - 1 && col == mid) || (row == mid && col == mid - 1) -> CellState.BLACK
                    else -> CellState.EMPTY
                }
            }
        }
        return Board(size, grid)
    }
}
