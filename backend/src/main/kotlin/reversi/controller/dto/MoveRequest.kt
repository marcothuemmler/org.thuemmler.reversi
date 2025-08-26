package reversi.controller.dto

import kotlinx.serialization.Serializable

@Serializable
data class MoveRequest(val row: Int, val col: Int)