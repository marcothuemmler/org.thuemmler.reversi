package reversi.service

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import reversi.ai.MoveSelectorStrategy
import reversi.controller.dto.MoveRequest
import reversi.controller.dto.NewGameRequest
import reversi.model.Board
import reversi.model.CellState
import reversi.model.Game
import reversi.model.PlayerType
import reversi.store.GameStore
import reversi.util.BoardFactory
import reversi.util.BoardUtil
import reversi.websocket.dto.MessageType
import java.util.UUID
import java.util.concurrent.Executors

@Service
class GameService(
    private val store: GameStore,
    val eventPublisher: GameEventPublisher = GameEventPublisher(),
    @param:Lazy private val moveSelector: MoveSelectorStrategy
) {

    private val aiDispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()

    fun createGame(game: NewGameRequest): Game {
        val board = BoardFactory.createStartingBoard()
        val createdGame = Game(
            id = game.id ?: UUID.randomUUID().toString(),
            board = board,
            playerTypes = game.playerTypes,
            currentPlayer = game.currentPlayer,
            validMoves = calculateValidMoves(board, game.currentPlayer).map { MoveRequest(it.first, it.second) }
        )
        var savedGame = saveState(createdGame, MessageType.CREATE)

        if (savedGame.playerTypes[savedGame.currentPlayer] == PlayerType.AI) {
            savedGame = handleAiTurn(savedGame)
        }

        return savedGame
    }

    fun listGames(): List<Game> = store.listGames()

    fun getGame(id: String): Game? = store.getGame(id)

    fun removeGame(id: String) = store.removeGame(id)

    fun saveState(game: Game, messageType: MessageType): Game {
        val savedGame = store.save(game)
        eventPublisher.notify(savedGame, messageType)
        return savedGame
    }

    fun makeMove(gameId: String, row: Int, col: Int): Game {
        var game = store.getGame(gameId) ?: throw NoSuchElementException("Game not found")

        if (game.playerTypes[game.currentPlayer] == PlayerType.AI) {
            throw IllegalStateException("It's AI's turn")
        }

        game = applyPlayerMove(game, row, col)

        val newGame = handleAiTurn(game)
        return newGame
    }

    private fun handleAiTurn(game: Game): Game = runBlocking(aiDispatcher) {
        var currentGame = game
        while (!currentGame.isFinished && currentGame.playerTypes[currentGame.currentPlayer] == PlayerType.AI) {
            val aiMove = moveSelector.selectMove(currentGame) ?: break
            delay(500)
            currentGame = applyPlayerMove(currentGame, aiMove.first, aiMove.second)
        }
        currentGame
    }

    private fun applyPlayerMove(game: Game, row: Int, col: Int): Game {
        val allFlippable = BoardUtil.directions.flatMap { (dx, dy) ->
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
            validMoves = calculateValidMoves(newBoard, nextPlayer).map { MoveRequest(it.first, it.second) },
            isFinished = finished
        )

        return saveState(updatedGame, MessageType.MAKE_MOVE)
    }

    fun getValidMoves(gameId: String): List<Pair<Int, Int>> {
        val game = store.getGame(gameId) ?: throw NoSuchElementException("Game not found")
        return calculateValidMoves(game.board, game.currentPlayer)
    }

    private fun calculateValidMoves(board: Board<CellState>, player: CellState): List<Pair<Int, Int>> {
        return buildList {
            for (row in 0 until board.size) {
                for (col in 0 until board.size) {
                    val flippable = BoardUtil.directions.flatMap { (dx, dy) ->
                        getFlippableCells(board, row, col, player, dx, dy)
                    }
                    if (board.getCell(row, col) == CellState.EMPTY && flippable.isNotEmpty()) {
                        add(row to col)
                    }
                }
            }
        }
    }

    private fun switchPlayer(currentPlayer: CellState) =
        if (currentPlayer == CellState.BLACK) CellState.WHITE else CellState.BLACK

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
                    BoardUtil.directions.any { (dx, dy) -> getFlippableCells(board, row, col, player, dx, dy).isNotEmpty() }
                ) return true
            }
        }
        return false
    }

    fun applyMove(board: Board<CellState>, row: Int, col: Int, player: CellState, flippable: List<Pair<Int, Int>>): Board<CellState> {
        return flippable.fold(board.setCell(row, col, player)) { b, (r, c) ->
            b.setCell(r, c, player)
        }
    }

    fun isGameFinished(board: Board<CellState>): Boolean {
        return !hasAnyValidMoves(board, CellState.BLACK) && !hasAnyValidMoves(board, CellState.WHITE)
    }

    @PreDestroy
    @Suppress("unused")
    fun shutdownAI() = aiDispatcher.close()

}
