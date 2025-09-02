package reversi.websocket.dto

import kotlinx.serialization.Serializable

@Serializable
data class ServerMessage<T>(
    val type: MessageType,
    val gameId: String? = null,
    val payload: T,
)
