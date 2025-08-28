package reversi.controller.dto

import kotlinx.serialization.Serializable
import reversi.model.CellState
import reversi.model.PlayerType

@Serializable
data class NewGameRequest(
    val id: String? = null,
    val playerTypes: Map<CellState, PlayerType>,
    val currentPlayer: CellState = CellState.BLACK
)
