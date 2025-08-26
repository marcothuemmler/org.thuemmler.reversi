package reversi.controller

import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.TextWebSocketHandler
import reversi.controller.dto.MoveRequest
import reversi.model.Game
import reversi.service.GameService
import reversi.util.MoveCommand
import reversi.util.UndoManager
import java.util.concurrent.ConcurrentHashMap

@Component
class GameWebSocketHandler(
    private val gameService: GameService,
    private val undoManagers: MutableMap<String, UndoManager>
) : TextWebSocketHandler() {

    private val sessions = ConcurrentHashMap<String, MutableSet<WebSocketSession>>()
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val gameId = session.uri?.query?.substringAfter("gameId=") ?: return
        undoManagers.getOrPut(gameId) { UndoManager() }
        sessions.computeIfAbsent(gameId) { mutableSetOf() }.add(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val gameId = session.uri?.query?.substringAfter("gameId=") ?: return
        val undoManager = undoManagers.getOrPut(gameId) { UndoManager() }
        val moveRequest = json.decodeFromString<MoveRequest>(message.payload)
        val command = MoveCommand(gameService, gameId, moveRequest.row, moveRequest.col) { updatedGame ->
            val gameWithValidMoves = updatedGame.copy(
                validMoves = gameService.getValidMoves(gameId).map { MoveRequest(it.first, it.second) }
            )
            broadcastGameUpdate(gameId, gameWithValidMoves)
        }
        undoManager.doStep(command)
    }

    private fun broadcastGameUpdate(gameId: String, game: Game) {
        val payload = json.encodeToString(Game.serializer(), game)
        sessions[gameId]?.forEach { session ->
            if (session.isOpen) session.sendMessage(TextMessage(payload))
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val gameId = session.uri?.query?.substringAfter("gameId=") ?: return
        sessions[gameId]?.remove(session)
        if (sessions[gameId]?.isEmpty() == true) {
            sessions.remove(gameId)
            undoManagers.remove(gameId)
        }
    }
}
