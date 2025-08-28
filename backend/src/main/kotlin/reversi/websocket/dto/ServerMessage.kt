package reversi.websocket.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ServerMessage(
    val type: MessageType,
    val gameId: String? = null,
    val payload: JsonObject? = null,
)
