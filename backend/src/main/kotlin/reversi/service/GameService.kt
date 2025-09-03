package reversi.service

import reversi.controller.dto.NewGameRequest
import reversi.model.Game
import reversi.websocket.dto.MessageType

interface GameService {
    fun createGame(game: NewGameRequest): Game
    fun listGames(): List<Game>
    fun getGame(id: String): Game?
    fun removeGame(id: String): Game?
    fun saveState(game: Game, messageType: MessageType): Game
    fun makeMove(gameId: String, row: Int, col: Int): Game
    fun subscribe(listener: (Game, MessageType) -> Unit)
    fun unsubscribe(listener: (Game, MessageType) -> Unit)
}
