package reversi.model

import kotlinx.serialization.Serializable

@Serializable
enum class CellState {
    EMPTY, BLACK, WHITE
}
