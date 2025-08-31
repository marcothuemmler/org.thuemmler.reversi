package reversi.websocket.dto

import kotlinx.serialization.Serializable
import reversi.controller.dto.MoveRequest
import reversi.model.Board
import reversi.model.CellState
import reversi.model.Game

@Serializable
data class GameUpdate(
    val id: String,
    val board: Board<CellState>,
    val currentPlayer: CellState,
    val validMoves: List<MoveRequest> = emptyList(),
    val isFinished: Boolean
) {
    companion object {
        fun fromGame(game: Game, includeValidMoves: Boolean = true): GameUpdate {
            return GameUpdate(
                id = game.id,
                board = game.board,
                currentPlayer = game.currentPlayer,
                validMoves = if (includeValidMoves) game.validMoves else emptyList(),
                isFinished = game.isFinished
            )
        }
    }
}
