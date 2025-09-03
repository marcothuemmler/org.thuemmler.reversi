package reversi.service

import org.springframework.stereotype.Component
import reversi.controller.dto.MoveRequest
import reversi.model.CellState
import reversi.model.Game
import reversi.model.GameBoard
import reversi.model.MoveList
import reversi.util.BoardUtil

@Component
class MoveEngineImpl : MoveEngine {

    override fun calculateValidMoves(board: GameBoard, player: CellState): MoveList {
        val validMoves = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                if (board.getCell(row, col) != CellState.EMPTY) continue
                if (BoardUtil.directions.any { (dx, dy) ->
                        getFlippableCells(board, row, col, player, dx, dy).isNotEmpty()
                    }) validMoves.add(row to col)
            }
        }
        return validMoves
    }

    override fun getFlippableCells(
        board: GameBoard, row: Int, col: Int, player: CellState, dx: Int, dy: Int
    ): MoveList {
        val flips = mutableListOf<Pair<Int, Int>>()
        var x = row + dx
        var y = col + dy

        while (x in 0 until board.size && y in 0 until board.size) {
            when (board.getCell(x, y)) {
                CellState.EMPTY -> return emptyList()
                player -> return flips.takeIf { it.isNotEmpty() } ?: emptyList()
                else -> flips.add(x to y)
            }
            x += dx
            y += dy
        }
        return emptyList()
    }

    override fun applyMove(board: GameBoard, row: Int, col: Int, player: CellState, flippable: MoveList): GameBoard {
        val updatesByRow = (flippable + (row to col)).groupBy { it.first }
        val newGrid = board.grid.mapIndexed { r, rowList ->
            updatesByRow[r]?.let { updates ->
                rowList.mapIndexed { c, cell -> if (updates.any { it.second == c }) player else cell }
            } ?: rowList
        }
        return board.copy(grid = newGrid)
    }

    override fun isGameFinished(board: GameBoard): Boolean {
        return !hasAnyValidMoves(board, CellState.BLACK) && !hasAnyValidMoves(board, CellState.WHITE)
    }

    override fun applyPlayerMove(game: Game, row: Int, col: Int): Game {
        val flippableByDirection = BoardUtil.directions.associateWith { (dx, dy) ->
            getFlippableCells(game.board, row, col, game.currentPlayer, dx, dy)
        }
        val allFlippable = flippableByDirection.values.flatten()

        if (game.board.getCell(row, col) != CellState.EMPTY || allFlippable.isEmpty()) {
            throw IllegalArgumentException("Invalid move")
        }

        val newBoard = applyMove(game.board, row, col, game.currentPlayer, allFlippable)
        var nextPlayer = if (game.currentPlayer == CellState.BLACK) CellState.WHITE else CellState.BLACK

        val nextPlayerValidMoves = calculateValidMoves(newBoard, nextPlayer)
        if (nextPlayerValidMoves.isEmpty() && hasAnyValidMoves(newBoard, game.currentPlayer)) {
            nextPlayer = game.currentPlayer
        }

        return game.copy(
            board = newBoard,
            currentPlayer = nextPlayer,
            validMoves = calculateValidMoves(newBoard, nextPlayer).map { MoveRequest(it.first, it.second) },
            isFinished = isGameFinished(newBoard)
        )
    }

    private fun hasAnyValidMoves(board: GameBoard, player: CellState): Boolean {
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                if (board.getCell(row, col) != CellState.EMPTY) continue
                if (BoardUtil.directions.any { (dx, dy) ->
                        getFlippableCells(board, row, col, player, dx, dy).isNotEmpty()
                    }) return true
            }
        }
        return false
    }
}
