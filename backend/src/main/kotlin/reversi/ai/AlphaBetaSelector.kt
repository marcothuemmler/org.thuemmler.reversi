package reversi.ai

import reversi.model.CellState
import reversi.model.Game
import reversi.model.GameBoard
import reversi.model.MoveList
import reversi.service.MoveEngine
import reversi.util.BoardUtil
import kotlin.math.max
import kotlin.math.min

class AlphaBetaSelector(
    private val moveEngine: MoveEngine,
    private val baseDepth: Int = 2
) : MoveSelectorStrategy {

    private val weightedBoard = arrayOf(
        arrayOf(99, -8, 8, 6, 6, 8, -8, 99),
        arrayOf(-8, -24, -4, -3, -3, -4, -24, -8),
        arrayOf(8, -4, 7, 4, 4, 7, -4, 8),
        arrayOf(6, -3, 4, 0, 0, 4, -3, 6),
        arrayOf(6, -3, 4, 0, 0, 4, -3, 6),
        arrayOf(8, -4, 7, 4, 4, 7, -4, 8),
        arrayOf(-8, -24, -4, -3, -3, -4, -24, -8),
        arrayOf(99, -8, 8, 6, 6, 8, -8, 99)
    )

    override fun selectMove(game: Game): Pair<Int, Int>? {
        val validMoves = moveEngine.calculateValidMoves(game.board, game.currentPlayer)
        if (validMoves.isEmpty()) return null

        val player = game.currentPlayer
        var bestMove: Pair<Int, Int>? = null
        var bestValue = Int.MIN_VALUE

        val orderedMoves = validMoves.sortedByDescending { weightedBoard[it.first][it.second] }

        val emptyCells = game.board.size * game.board.size - game.board.grid.sumOf { row -> row.count { it != CellState.EMPTY } }
        val depth = dynamicDepth(emptyCells)

        for (move in orderedMoves) {
            val newBoard = simulateMove(game.board, player, move)
            val value = alphaBeta(newBoard, switchPlayer(player), depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, player)
            if (value > bestValue) {
                bestValue = value
                bestMove = move
            }
        }

        return bestMove
    }

    private fun dynamicDepth(emptyCells: Int): Int {
        return when {
            emptyCells <= 15 -> baseDepth + 2 // deeper in endgame
            emptyCells <= 30 -> baseDepth + 1
            else -> baseDepth
        }
    }

    private fun alphaBeta(
        board: GameBoard,
        currentPlayer: CellState,
        depth: Int,
        alpha: Int,
        beta: Int,
        aiPlayer: CellState
    ): Int {
        if (moveEngine.isGameFinished(board)) {
            val diff = count(aiPlayer, board) - count(switchPlayer(aiPlayer), board)
            return diff * 10000
        }
        if (depth == 0) {
            return evaluate(board, aiPlayer)
        }

        var a = alpha
        var b = beta
        val validMoves = getValidMoves(board, currentPlayer).let { moves ->
            if (currentPlayer == aiPlayer) {
                moves.sortedByDescending { weightedBoard[it.first][it.second] }
            } else {
                moves.sortedBy { weightedBoard[it.first][it.second] }
            }
        }
        if (validMoves.isEmpty()) {
            // pass turn
            return alphaBeta(board, switchPlayer(currentPlayer), depth - 1, a, b, aiPlayer)
        }

        val maximizingPlayer = currentPlayer == aiPlayer

        if (maximizingPlayer) {
            var maxEval = Int.MIN_VALUE
            for (move in validMoves) {
                val newBoard = simulateMove(board, currentPlayer, move)
                val eval = alphaBeta(newBoard, switchPlayer(currentPlayer), depth - 1, a, b, aiPlayer)
                maxEval = max(maxEval, eval)
                a = max(a, eval)
                if (b <= a) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in validMoves) {
                val newBoard = simulateMove(board, currentPlayer, move)
                val eval = alphaBeta(newBoard, switchPlayer(currentPlayer), depth - 1, a, b, aiPlayer)
                minEval = min(minEval, eval)
                b = min(b, eval)
                if (b <= a) break
            }
            return minEval
        }
    }

    private fun evaluate(board: GameBoard, aiPlayer: CellState): Int {
        val opponent = switchPlayer(aiPlayer)
        var score = 0

        // Weighted position score
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                when (board.getCell(row, col)) {
                    aiPlayer -> score += weightedBoard[row][col]
                    opponent -> score -= weightedBoard[row][col]
                    else -> {}
                }
            }
        }

        // Mobility
        score += 10 * (getValidMoves(board, aiPlayer).size - getValidMoves(board, opponent).size)

        // Stability (only corners + edges for speed)
        score += 20 * (countStablePieces(board, aiPlayer) - countStablePieces(board, opponent))

        return score
    }

    private fun getValidMoves(board: GameBoard, player: CellState): MoveList {
        return buildList {
            for (row in 0 until board.size) {
                for (col in 0 until board.size) {
                    val flips = BoardUtil.directions.flatMap { (dx, dy) ->
                        moveEngine.getFlippableCells(board, row, col, player, dx, dy)
                    }
                    if (board.getCell(row, col) == CellState.EMPTY && flips.isNotEmpty()) add(row to col)
                }
            }
        }
    }

    private fun simulateMove(board: GameBoard, player: CellState, move: Pair<Int, Int>): GameBoard {
        val flippable = BoardUtil.directions.flatMap { (dx, dy) ->
            moveEngine.getFlippableCells(board, move.first, move.second, player, dx, dy)
        }
        return moveEngine.applyMove(board, move.first, move.second, player, flippable)
    }

    private fun countStablePieces(board: GameBoard, player: CellState): Int {
        val size = board.size
        val stable = Array(size) { BooleanArray(size) }
        val corners = listOf(0 to 0, 0 to size - 1, size - 1 to 0, size - 1 to size - 1)

        // Only consider lines from corners for faster approximation
        corners.forEach { (r, c) ->
            if (board.getCell(r, c) == player) {
                listOf(1 to 0, 0 to 1, -1 to 0, 0 to -1).forEach { (dr, dc) ->
                    var row = r
                    var col = c
                    while (row in 0 until size && col in 0 until size && board.getCell(row, col) == player) {
                        stable[row][col] = true
                        row += dr
                        col += dc
                    }
                }
            }
        }

        return stable.sumOf { it.count { b -> b } }
    }

    private fun switchPlayer(player: CellState) =
        if (player == CellState.BLACK) CellState.WHITE else CellState.BLACK

    private fun count(player: CellState, board: GameBoard): Int = board.grid.sumOf { row -> row.count { it == player } }
}
