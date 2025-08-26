package reversi.ai

import reversi.model.Board
import reversi.model.CellState
import reversi.model.Game
import reversi.service.GameService
import reversi.service.MoveSelectorStrategy
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.delay

class AlphaBetaSelector(
    private val gameService: GameService,
    private val baseDepth: Int = 1
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

    override suspend fun selectMove(game: Game): Pair<Int, Int>? {
        val startTime = System.currentTimeMillis()

        val validMoves = gameService.getValidMoves(game.id)
        if (validMoves.isEmpty()) return null

        val player = game.currentPlayer
        var bestMove: Pair<Int, Int>? = null
        var bestValue = Int.MIN_VALUE

        val orderedMoves = validMoves.sortedByDescending { weightedBoard[it.first][it.second] }

        val emptyCells = game.board.size * game.board.size - game.board.grid.sumOf { row -> row.count { it != CellState.EMPTY } }
        val depth = dynamicDepth(emptyCells)

        for (move in orderedMoves) {
            val newBoard = simulateMove(game.board, player, move)
            val value = alphaBeta(newBoard, switchPlayer(player), depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, false, player)
            if (value > bestValue) {
                bestValue = value
                bestMove = move
            }
        }

        val elapsed = System.currentTimeMillis() - startTime
        val remaining = 500 - elapsed
        if (remaining > 0) {
            delay(remaining)
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
        board: Board<CellState>,
        currentPlayer: CellState,
        depth: Int,
        alpha: Int,
        beta: Int,
        maximizingPlayer: Boolean,
        aiPlayer: CellState
    ): Int {
        if (depth == 0 || gameService.isGameFinished(board)) {
            return evaluate(board, aiPlayer)
        }

        var a = alpha
        var b = beta
        val validMoves = getValidMoves(board, currentPlayer)
        if (validMoves.isEmpty()) {
            // pass turn
            return alphaBeta(board, switchPlayer(currentPlayer), depth - 1, a, b, !maximizingPlayer, aiPlayer)
        }

        if (maximizingPlayer) {
            var maxEval = Int.MIN_VALUE
            for (move in validMoves) {
                val newBoard = simulateMove(board, currentPlayer, move)
                val eval = alphaBeta(newBoard, switchPlayer(currentPlayer), depth - 1, a, b, false, aiPlayer)
                maxEval = max(maxEval, eval)
                a = max(a, eval)
                if (b <= a) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in validMoves) {
                val newBoard = simulateMove(board, currentPlayer, move)
                val eval = alphaBeta(newBoard, switchPlayer(currentPlayer), depth - 1, a, b, true, aiPlayer)
                minEval = min(minEval, eval)
                b = min(b, eval)
                if (b <= a) break
            }
            return minEval
        }
    }

    private fun evaluate(board: Board<CellState>, aiPlayer: CellState): Int {
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

    private fun getValidMoves(board: Board<CellState>, player: CellState): List<Pair<Int, Int>> {
        return buildList {
            for (row in 0 until board.size) {
                for (col in 0 until board.size) {
                    val flips = gameService.directions.flatMap { (dx, dy) ->
                        gameService.getFlippableCells(board, row, col, player, dx, dy)
                    }
                    if (board.getCell(row, col) == CellState.EMPTY && flips.isNotEmpty()) add(row to col)
                }
            }
        }
    }

    private fun simulateMove(board: Board<CellState>, player: CellState, move: Pair<Int, Int>): Board<CellState> {
        val flippable = gameService.directions.flatMap { (dx, dy) ->
            gameService.getFlippableCells(board, move.first, move.second, player, dx, dy)
        }
        return gameService.applyMove(board, move.first, move.second, player, flippable)
    }

    private fun countStablePieces(board: Board<CellState>, player: CellState): Int {
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
}
