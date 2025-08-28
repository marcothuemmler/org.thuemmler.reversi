package reversi.websocket.dto

import kotlinx.serialization.Serializable

@Serializable
enum class MessageType {
    CREATE,
    JOIN,
    MAKE_MOVE,
    UNDO,
    REDO,
    ERROR
}
