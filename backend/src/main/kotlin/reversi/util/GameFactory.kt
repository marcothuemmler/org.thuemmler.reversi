package reversi.util

import reversi.controller.dto.MoveRequest
import reversi.controller.dto.NewGameRequest
import reversi.model.Game
import reversi.model.GameBoard
import reversi.model.MoveList
import java.util.*

object GameFactory {
    fun createNewGame(game: NewGameRequest, board: GameBoard?, validMoves: MoveList = emptyList()): Game {
        return Game(
            id = game.id ?: UUID.randomUUID().toString(),
            difficulty = game.difficulty,
            board = board ?: BoardFactory.createStartingBoard(),
            playerTypes = game.playerTypes,
            currentPlayer = game.currentPlayer,
            validMoves = validMoves.map { MoveRequest(it.first, it.second) }
        )
    }
}