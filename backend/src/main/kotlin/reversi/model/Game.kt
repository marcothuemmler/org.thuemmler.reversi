package reversi.model

import kotlinx.serialization.Serializable
import reversi.controller.dto.MoveRequest
import java.util.UUID

@Serializable
data class Game(
    val id: String = UUID.randomUUID().toString(),
    val board: Board<CellState>,
    val currentPlayer: CellState = CellState.BLACK,
    val validMoves: List<MoveRequest> = emptyList(),
    val playerTypes: Map<CellState, PlayerType> = mapOf(
        CellState.BLACK to PlayerType.HUMAN,
        CellState.WHITE to PlayerType.AI
    ),
    val isFinished: Boolean = false
)

enum class PlayerType { HUMAN, AI }