package reversi.controller.dto

import kotlinx.serialization.Serializable
import reversi.model.CellState
import reversi.model.Difficulty
import reversi.model.PlayerType

@Serializable
data class NewGameRequest(
    val id: String? = null,
    val difficulty: Difficulty,
    val playerTypes: Map<CellState, PlayerType>,
    val currentPlayer: CellState = CellState.BLACK,
    val preferredSide: CellState? = CellState.BLACK
)
