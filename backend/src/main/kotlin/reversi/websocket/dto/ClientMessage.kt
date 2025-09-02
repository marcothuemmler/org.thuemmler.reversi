package reversi.websocket.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import reversi.controller.dto.MoveRequest
import reversi.controller.dto.NewGameRequest

@Serializable
sealed interface ClientMessage

@Serializable
@SerialName("MAKE_MOVE")
data class MakeMove(
    val type: MessageType = MessageType.MAKE_MOVE,
    val gameId: String,
    val payload: MoveRequest
) : ClientMessage

@Serializable
@SerialName("CREATE")
data class CreateGame(
    val type: MessageType = MessageType.CREATE,
    val payload: NewGameRequest
) : ClientMessage

@Serializable
@SerialName("UNDO")
data class Undo(
    val type: MessageType = MessageType.UNDO,
    val gameId: String
) : ClientMessage

@Serializable
@SerialName("REDO")
data class Redo(
    val type: MessageType = MessageType.REDO,
    val gameId: String
) : ClientMessage

@Serializable
@SerialName("JOIN")
data class Join(
    val type: MessageType = MessageType.JOIN,
    val gameId: String
) : ClientMessage
