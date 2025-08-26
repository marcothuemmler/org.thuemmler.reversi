package reversi.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import reversi.ai.AlphaBetaSelector
import reversi.controller.dto.MoveRequest
import reversi.model.Board
import reversi.model.CellState
import reversi.model.Game
import reversi.model.PlayerType
import reversi.store.GameStore

@Service
class GameService(
    private val store: GameStore
) {

    private val moveSelector: MoveSelectorStrategy = AlphaBetaSelector(this)
    private val aiScope = CoroutineScope(Dispatchers.Default)
    val directions = listOf(
        -1 to -1, -1 to 0, -1 to 1,
        0 to -1,          0 to 1,
        1 to -1,  1 to 0, 1 to 1
    )

    fun createGame(): Game {
        val game = Game(board = createStartingCellStateBoard())
        return store.save(game)
    }

    fun listGames(): List<Game> = store.listGames()

    fun getGame(id: String): Game? = store.getGame(id)

    fun removeGame(id: String) = store.removeGame(id)

    fun saveState(game: Game) = store.save(game)

    fun makeMove(gameId: String, row: Int, col: Int, onUpdate: ((Game) -> Unit)? = null): Game {
        var game = store.getGame(gameId) ?: throw NoSuchElementException("Game not found")

        if (game.playerTypes[game.currentPlayer] == PlayerType.AI) {
            throw IllegalStateException("It's AI's turn")
        }

        game = applyPlayerMove(game, row, col)
        onUpdate?.invoke(game)

        aiScope.launch {
            while (!game.isFinished && game.playerTypes[game.currentPlayer] == PlayerType.AI) {
                val aiMove = aiMove(game) ?: break
                game = applyPlayerMove(game, aiMove.first, aiMove.second)
                onUpdate?.invoke(game)
            }
        }

        return game
    }

    private fun applyPlayerMove(game: Game, row: Int, col: Int): Game {
        val allFlippable = directions.flatMap { (dx, dy) ->
            getFlippableCells(game.board, row, col, game.currentPlayer, dx, dy)
        }

        if (game.board.getCell(row, col) != CellState.EMPTY || allFlippable.isEmpty()) {
            throw IllegalArgumentException("Invalid move")
        }

        val newBoard = applyMove(game.board, row, col, game.currentPlayer, allFlippable)
        var nextPlayer = switchPlayer(game.currentPlayer)
        if (!hasAnyValidMoves(newBoard, nextPlayer) && hasAnyValidMoves(newBoard, game.currentPlayer)) {
            nextPlayer = game.currentPlayer
        }

        val finished = isGameFinished(newBoard)

        val updatedGame = game.copy(
            board = newBoard,
            currentPlayer = nextPlayer,
            validMoves = getValidMoves(game.id).map { MoveRequest(it.first, it.second) },
            isFinished = finished
        )

        return store.save(updatedGame)
    }


    fun getValidMoves(gameId: String): List<Pair<Int, Int>> {
        val game = store.getGame(gameId) ?: throw NoSuchElementException("Game not found")
        val player = game.currentPlayer

        return buildList {
            for (row in 0 until game.board.size) {
                for (col in 0 until game.board.size) {
                    val flippable = directions.flatMap { (dx, dy) ->
                        getFlippableCells(game.board, row, col, player, dx, dy)
                    }
                    if (game.board.getCell(row, col) == CellState.EMPTY && flippable.isNotEmpty()) {
                        add(row to col)
                    }
                }
            }
        }
    }

    private fun switchPlayer(currentPlayer: CellState) =
        if (currentPlayer == CellState.BLACK) CellState.WHITE else CellState.BLACK

    private fun createStartingCellStateBoard(): Board<CellState> {
        val size = 8
        val mid = size / 2
        val grid = List(size) { i ->
            List(size) { j ->
                when {
                    (i == mid && j == mid) || (i == mid - 1 && j == mid - 1) -> CellState.BLACK
                    (i == mid && j == mid - 1) || (i == mid - 1 && j == mid) -> CellState.WHITE
                    else -> CellState.EMPTY
                }
            }
        }
        return Board(grid = grid)
    }

    fun getFlippableCells(
        board: Board<CellState>,
        row: Int,
        col: Int,
        player: CellState,
        dx: Int,
        dy: Int
    ): List<Pair<Int, Int>> {
        val flips = mutableListOf<Pair<Int, Int>>()
        var x = row + dx
        var y = col + dy

        while (x in 0 until board.size && y in 0 until board.size) {
            when (board.getCell(x, y)) {
                CellState.EMPTY -> return emptyList()
                player -> return flips
                else -> flips.add(x to y)
            }
            x += dx
            y += dy
        }
        return emptyList()
    }

    private fun hasAnyValidMoves(board: Board<CellState>, player: CellState): Boolean {
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                if (board.getCell(row, col) == CellState.EMPTY &&
                    directions.any { (dx, dy) -> getFlippableCells(board, row, col, player, dx, dy).isNotEmpty() }
                ) return true
            }
        }
        return false
    }

    fun applyMove(board: Board<CellState>, row: Int, col: Int, player: CellState, flippable: List<Pair<Int, Int>>): Board<CellState> {
        var newBoard = board.setCell(row, col, player)
        flippable.forEach { (r, c) -> newBoard = newBoard.setCell(r, c, player) }
        return newBoard
    }

    fun isGameFinished(board: Board<CellState>): Boolean {
        return !hasAnyValidMoves(board, CellState.BLACK) && !hasAnyValidMoves(board, CellState.WHITE)
    }

    private suspend fun aiMove(game: Game): Pair<Int, Int>? {
        return moveSelector.selectMove(game)
    }
}
