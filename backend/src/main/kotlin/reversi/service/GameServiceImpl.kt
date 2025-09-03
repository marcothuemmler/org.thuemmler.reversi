package reversi.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import reversi.ai.MoveSelectorStrategy
import reversi.controller.dto.NewGameRequest
import reversi.model.Game
import reversi.model.PlayerType
import reversi.store.GameStore
import reversi.util.BoardFactory
import reversi.util.GameFactory
import reversi.websocket.dto.MessageType

@Service
class GameServiceImpl(
    private val store: GameStore,
    private val eventPublisher: GameEventPublisher,
    @param:Lazy private val moveSelector: MoveSelectorStrategy,
    private val moveEngine: MoveEngine
) : GameService {

    override fun createGame(game: NewGameRequest): Game {
        val board = BoardFactory.createStartingBoard()
        val validMoves = moveEngine.calculateValidMoves(board, game.currentPlayer)
        val createdGame = GameFactory.createNewGame(game, board, validMoves)
        var savedGame = saveState(createdGame, MessageType.CREATE)

        if (savedGame.playerTypes[savedGame.currentPlayer] == PlayerType.AI) {
            savedGame = handleAiTurn(savedGame)
        }

        return savedGame
    }

    override fun listGames(): List<Game> = store.listGames()
    override fun getGame(id: String): Game? = store.getGame(id)
    override fun removeGame(id: String) = store.removeGame(id)
    override fun subscribe(listener: (Game, MessageType) -> Unit) = eventPublisher.subscribe(listener)
    override fun unsubscribe(listener: (Game, MessageType) -> Unit) = eventPublisher.unsubscribe(listener)

    override fun saveState(game: Game, messageType: MessageType): Game {
        val savedGame = store.save(game)
        eventPublisher.notify(savedGame, messageType)
        return savedGame
    }

    override fun makeMove(gameId: String, row: Int, col: Int): Game {
        var game = store.getGame(gameId) ?: throw NoSuchElementException("Game not found")

        if (game.playerTypes[game.currentPlayer] == PlayerType.AI) {
            throw IllegalStateException("It's AI's turn")
        }

        game = applyMove(game, row, col)
        return handleAiTurn(game)
    }

    private fun handleAiTurn(game: Game): Game = runBlocking {
        var currentGame = game
        while (!currentGame.isFinished && currentGame.playerTypes[currentGame.currentPlayer] == PlayerType.AI) {
            val aiMove = moveSelector.selectMove(currentGame) ?: break
            delay(500)
            currentGame = applyMove(currentGame, aiMove.first, aiMove.second)
        }
        currentGame
    }

    private fun applyMove(game: Game, row: Int, col: Int): Game {
        val updatedGame = moveEngine.applyPlayerMove(game, row, col)
        return saveState(updatedGame, MessageType.MAKE_MOVE)
    }
}
