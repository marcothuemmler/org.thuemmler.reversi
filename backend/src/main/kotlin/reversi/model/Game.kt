package reversi.model

import kotlinx.serialization.Serializable
import reversi.controller.dto.MoveRequest
import java.util.*

@Serializable
data class Game(
    val id: String = UUID.randomUUID().toString(),
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val board: GameBoard,
    val currentPlayer: CellState = CellState.BLACK,
    val validMoves: List<MoveRequest> = emptyList(),
    val playerTypes: Map<CellState, PlayerType> = mapOf(
        CellState.BLACK to PlayerType.HUMAN,
        CellState.WHITE to PlayerType.AI
    ),
    val isFinished: Boolean = false
)

enum class PlayerType { HUMAN, AI }
enum class Difficulty { EASY, MEDIUM, HARD }